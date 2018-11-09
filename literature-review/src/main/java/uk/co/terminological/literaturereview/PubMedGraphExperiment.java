package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntriesToNode;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapHasReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapHasRelated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents.Events;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
import uk.co.terminological.pubmedclient.BibliographicApiException;
import uk.co.terminological.pubmedclient.BibliographicApis;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;
import uk.co.terminological.pubmedclient.CrossRefResult.Work;
import uk.co.terminological.pubmedclient.EntrezClient.Command;
import uk.co.terminological.pubmedclient.EntrezClient.Database;
import  uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMedGraphExperiment {

	static Logger log = LoggerFactory.getLogger(PubMedGraphExperiment.class);


	// Event types
	static final String PUBMED_SEARCH_RESULT = "PubMed search result";
	static final String PUBMED_FETCH_RESULT = "PubMed fetch result";
	static final String PMID_DOI_MAP = "PubMed to Doi";

	static final String XREF_FETCH_RESULT = "CrossRef metadata entry";
	static final String XREF_REFERENCES_FOR_DOI = "CrossRef reference list";

	//Event names
	//static final String PMID_LIST = "PubMed ids";
	//static final String DOI_LIST = "DOIs";
	//public static final String PUBMED_ENTRY_AVAILABLE = "PubMed Entry";

	// Handlers & Generators
	public static final String PUBMED_SEARCHER = "PubMed eSearch";
	public static final String PUBMED_LINKER = "PubMed eLink";

	public static final String PUBMED_FETCHER = "PubMed eFetch";
	public static final String XREF_LOOKUP = "Crossref lookup";
	public static final String DOI_EXPANDER = "Doi reverse lookup";
	public static final String PMID_EXPANDER = "Convert similar to search result";

	public static final String NEO4J_WRITER = "Neo4j pubmed node writer";

	// Metadata keys

	

	public static void main(String args[]) throws IOException {

		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);

		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(propPath));

		Path secretsPath = fromProperty(prop,"bibliography-secrets");
		Path graphOptionsPath = fromProperty(prop,"graph-db-directory");
		Path workingDir = fromProperty(prop,"working-directory");
		Path outputDir = fromProperty(prop,"output-directory");
		if (!Files.exists(graphOptionsPath)) Files.createDirectories(graphOptionsPath);
		if (!Files.exists(workingDir)) Files.createDirectories(workingDir);
		if (!Files.exists(outputDir)) Files.createDirectories(outputDir);



		BibliographicApis biblioApi = BibliographicApis.create(secretsPath);
		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphOptionsPath);

		String search = prop.getProperty("search");
		String broaderSearch = prop.getProperty("broader-search");
		Integer maxDepth = Integer.parseInt(prop.getProperty("max-depth"));

		execute(graphApi, biblioApi, workingDir, outputDir, search, broaderSearch, maxDepth);

		graphApi.waitAndShutdown();
	}

	static Path fromProperty(Properties prop, String name) {
		return Paths.get( 
				prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}
	
	public static class PMIDList extends ArrayList<String> {}

	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi, Path workingDir, Path outputDir, String search, String broaderSearch, Integer maxDepth) throws IOException {

		log.error("Starting graphDb build");
		PubMedGraphSchema.setupSchema(graphApi);

		EventBus.get()
		.withApi(graphApi)
		.withApi(biblioApi)
		.withApi(new PMIDList())
		.withEventGenerator(searchPubMed(search))
		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(PMID_STUB))
		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(DOI_STUB))
		.withHandler(expandDoiStubs())
		.withHandler(expandPMIDStubs())
		.withHandler(fetchPubMedEntries())
		.withHandler(fetchCrossRefFromPubMed(maxDepth))
		.withHandler(findCrossRefReferences(maxDepth))
		.withHandler(findRelatedArticlesFromPMIDs(maxDepth,broaderSearch))
		.debugMode()
		.execute()
		.sendShutdownMessage()
		.writeExecutionGraphs(outputDir)
		.shutdown();
		;

	}

	static EventGenerator<List<String>> searchPubMed(String search) {
		return Generators.generator(search, 
				PUBMED_SEARCHER, 
				g -> {
					try {
						List<String> tmp = g.getEventBus().getApi(BibliographicApis.class).get()
								.getEntrez().findPMIdsBySearch(search);
						/*tmp.forEach(pmid ->
								g.getEventBus().getApi(StatusRecord.class).get()
									.add(pmid, Collections.emptyMap())
							);*/
						return tmp;
					} catch (BibliographicApiException e) {
						g.getEventBus().handleException(e);
						return Collections.emptyList();
					}
				},
				name -> search, 
				type -> PUBMED_SEARCH_RESULT);
	}

	static EventProcessor<Set<Long>> expandDoiStubs() {
		return Handlers.eventProcessor(DOI_EXPANDER, 
				Predicates.matchNameAndType(DOI_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
				(event,context) -> {
					BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
					Integer depth = Optional.ofNullable((Integer) event.get("depth")).orElse(0);
					Set<Long> nodeIds = event.get();
					List<String> dois = new ArrayList<>();
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					try  ( Transaction tx = graph.get().beginTx() ) {
						nodeIds.forEach(id -> {
							Node n = graph.get().getNodeById(id);
							Optional.ofNullable(n.getProperty("doi",null)).ifPresent(
									doi -> dois.add(doi.toString()));
						});
						tx.success();
					}
					try {
						List<String> pmids = bib.getEntrez().findPMIdsByDois(dois);
						context.send(
								Events.typedEvent(pmids,type -> PUBMED_SEARCH_RESULT).put("depth",depth)
								);
					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});
	}

	static EventProcessor<Set<Long>> expandPMIDStubs() {
		return Handlers.eventProcessor(PMID_EXPANDER, 
				Predicates.matchNameAndType(PMID_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
				(event,context) -> {
					Integer depth = Optional.ofNullable((Integer) event.get("depth")).orElse(0);
					Set<Long> nodeIds = event.get();
					List<String> pubMedIds = new ArrayList<>();
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					try  ( Transaction tx = graph.get().beginTx() ) {
						nodeIds.forEach(id -> {
							Node n = graph.get().getNodeById(id);
							Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
									pmid -> pubMedIds.add(pmid.toString()));
						});
						tx.success();
					}
					context.send(
							Events.typedEvent(pubMedIds,type -> PUBMED_SEARCH_RESULT).put("depth",depth)
							);
				});
	}

	static EventProcessor<List<String>> findRelatedArticlesFromPMIDs(Integer maxDepth,String searchWithin) {
		return Handlers.eventProcessor(PUBMED_LINKER, 
				Predicates.matchType(PUBMED_SEARCH_RESULT)
				.and(ev -> Optional.ofNullable((Integer) ev.get("depth")).orElse(0) < maxDepth), 

				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
						Integer depth = Optional.ofNullable((Integer) event.get("depth")).orElse(0);
						Stream<Link> tmp = bib.getEntrez()
								.buildLinksQueryForIdsAndDatabase(event.get(), Database.PUBMED)
								.command(Command.NEIGHBOR_SCORE)
								.withLinkname("pubmed_pubmed")
								.searchLinked(searchWithin)
								.execute().stream();
						tmp.forEach(link -> { 
							link.toId.ifPresent(toId -> mapHasRelated(link.fromId, toId, link.score.orElse(0L), depth, graph));
						});


					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});

	}

	static EventProcessor<List<String>> fetchPubMedEntries() {
		return Handlers.eventProcessor(PUBMED_FETCHER, 
				Predicates.matchType(PUBMED_SEARCH_RESULT), 
				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
						Integer depth = Optional.ofNullable((Integer) event.get("depth")).orElse(0);

						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();

						PubMedEntries entries = bib.getEntrez()
								.getPMEntriesByPMIds(event.get());

						mapEntriesToNode(entries, depth, graph);
						
						entries.stream().forEach(entry -> {
							
							context.send(
									//Add a depth parameter to the event 
									Events.namedTypedEvent(entry, entry.getPMID().get(), PUBMED_FETCH_RESULT)
									.put("depth",depth+1)
									);
						});

						Map<String,String> pmids2dois = new HashMap<>();
						entries.stream().forEach(entry -> {
							if (entry.getPMID().isPresent() && entry.getDoi().isPresent()) {
								pmids2dois.put(entry.getPMID().get(), entry.getDoi().get());
							}
						});
						context.send(Events.typedEvent(pmids2dois, type -> PMID_DOI_MAP));


					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});
	}




	static EventProcessor<PubMedEntry> fetchCrossRefFromPubMed(Integer maxDepth) {
		return Handlers.eventProcessor(
				XREF_LOOKUP, 
				Predicates
				.matchType(PUBMED_FETCH_RESULT)
				.and(ev -> Optional.ofNullable((Integer) ev.get("depth")).orElse(0) < maxDepth
						), 
				(entry,context) -> {
					BibliographicApis api = context.getEventBus().getApi(BibliographicApis.class).get();
					Optional<String> optDoi = entry.get().getDoi();
					if (!optDoi.isPresent()) return;

					Integer depth = Optional.ofNullable((Integer) entry.get("depth")).orElse(0);

					try {
						SingleResult tmp = api.getCrossref().getByDoi(optDoi.get());
						tmp.work.ifPresent(work -> context.send(
								Events.namedTypedEvent(work,optDoi.get(),XREF_FETCH_RESULT)
								.put("depth", depth+1)
								));

					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});
	}

	static EventProcessor<Work> findCrossRefReferences(Integer maxDepth) {
		return Handlers.eventProcessor(
				XREF_LOOKUP, 
				Predicates
				.matchType(XREF_FETCH_RESULT)
				.and(ev -> Optional.ofNullable((Integer) ev.get("depth")).orElse(0) < maxDepth), 
				(entry,context) -> {
					Integer depth = Optional.ofNullable((Integer) entry.get("depth")).orElse(0);
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					Optional<String> optDoi = entry.getMetadata().name();
					List<String> referencedDois = entry.get().reference.stream()
							.flatMap(r -> r.DOI.stream())
							.collect(Collectors.toList());
					mapHasReferences(optDoi.get(),referencedDois,depth,graph);
				});
	}



}
