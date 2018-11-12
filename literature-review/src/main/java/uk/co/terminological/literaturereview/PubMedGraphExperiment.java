package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
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
	public static final String PMID_EXPANDER = "Similarity search PMID";

	public static final String NEO4J_WRITER = "Neo4j pubmed node writer";

	// Metadata keys

	static Node lockNode;

	public static void main(String args[]) throws IOException {

		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);

		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(propPath));

		prop.forEach((k,v) -> prop.put(k, v.toString().replace("~", System.getProperty("user.home")))); 
		
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

		// execute(graphApi, biblioApi, workingDir, outputDir, search, broaderSearch, maxDepth);

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

		try ( Transaction tx = graphApi.get().beginTx() ) {
			lockNode = graphApi.get().createNode();
		}
		
		EventBus.get()
		.withApi(graphApi)
		.withApi(biblioApi)
		.withApi(new PMIDList())
		.withEventGenerator(searchPubMed(search))
		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(PMID_STUB))
		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(DOI_STUB))
		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(EXPAND))
		.withHandler(expandDOIStubs())
		.withHandler(expandPMIDStubs())
		.withHandler(fetchPubMedEntries(maxDepth))
		.withHandler(findCrossRefReferencesFromNodes())
		.withHandler(findRelatedArticlesFromNodes(broaderSearch))
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
						g.getEventBus().logInfo("Pubmed search found: "+tmp.size()+" results");
						return tmp;
					} catch (BibliographicApiException e) {
						g.getEventBus().handleException(e);
						return Collections.emptyList();
					}
				},
				name -> search, 
				type -> PUBMED_SEARCH_RESULT);
	}

	static EventProcessor<Set<Long>> expandDOIStubs() {
		return Handlers.eventProcessor(DOI_EXPANDER, 
				Predicates.matchNameAndType(DOI_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
				(event,context) -> {
					BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
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
						context.getEventBus().logInfo("Looked up "+nodeIds.size()+" dois and found "+pmids.size()+" pubmed records");
						context.send(
								Events.typedEvent(pmids,type -> PUBMED_SEARCH_RESULT)
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
					Set<Long> nodeIds = event.get();
					List<String> pubMedIds = new ArrayList<>();
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					try  ( Transaction tx = graph.get().beginTx() ) {
						tx.acquireWriteLock(lockNode);
						nodeIds.forEach(id -> {
							Node n = graph.get().getNodeById(id);
							Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
									pmid -> pubMedIds.add(pmid.toString()));
						});
						tx.success();
					}
					context.getEventBus().logInfo("Looked up "+pubMedIds.size()+" from related pubmed records");
					context.send(
							Events.typedEvent(pubMedIds,type -> PUBMED_SEARCH_RESULT)
							);
				});
	}

	

	static EventProcessor<List<String>> fetchPubMedEntries(Integer maxDepth) {
		return Handlers.eventProcessor(PUBMED_FETCHER, 
				Predicates.matchType(PUBMED_SEARCH_RESULT), 
				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
						
						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();

						PubMedEntries entries = bib.getEntrez()
								.getPMEntriesByPMIds(event.get());

						mapEntriesToNode(entries, graph,maxDepth);
						//so the problem is that  need to make a decision about whether to expand based on 
						//depth. nodes created above will have a EXPAND label.
						//However crossref lookup if on an individual
						entries.stream().forEach(entry -> {
							
							context.send(
									Events.namedTypedEvent(entry, entry.getPMID().get(), PUBMED_FETCH_RESULT)
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


	static EventProcessor<Set<Long>> findRelatedArticlesFromNodes(String searchWithin) {
		return Handlers.eventProcessor(PUBMED_LINKER, 
				Predicates.matchNameAndType(EXPAND.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE),
				
				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
						
						List<String> pmids = new ArrayList<>();
						try  ( Transaction tx = graph.get().beginTx() ) {
							tx.acquireWriteLock(lockNode);
							event.get().forEach(id -> {
								Node n = graph.get().getNodeById(id);
								Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
										pmid -> pmids.add(pmid.toString()));
							});
							tx.success();
						}
						
						context.getEventBus().logInfo("");
						
						List<Link> tmp = bib.getEntrez()
								.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
								.command(Command.NEIGHBOR_SCORE)
								.withLinkname("pubmed_pubmed")
								.searchLinked(searchWithin)
								.execute().stream()
								.flatMap(o -> o.stream()).collect(Collectors.toList());
						
						mapHasRelated(tmp, graph);

					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});

	}

	static EventProcessor<Set<Long>> findCrossRefReferencesFromNodes() {
		return Handlers.eventProcessor(
				XREF_LOOKUP, 
				Predicates.matchNameAndType(EXPAND.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
				(event,context) -> {
					BibliographicApis api = context.getEventBus().getApi(BibliographicApis.class).get();
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					
					List<String> dois = new ArrayList<>();
					try  ( Transaction tx = graph.get().beginTx() ) {
						tx.acquireWriteLock(lockNode);
						event.get().forEach(id -> {
							Node n = graph.get().getNodeById(id);
							Optional.ofNullable(n.getProperty("doi",null)).ifPresent(
									doi -> dois.add(doi.toString()));
						});
						tx.success();
					}
					
					for (String doi: dois) {
						try {
							SingleResult tmp = api.getCrossref().getByDoi(doi);
							tmp.work.ifPresent(work -> context.send(
									Events.namedTypedEvent(work,doi,XREF_FETCH_RESULT)
								));
							List<String> referencedDois = tmp.work.stream()
								.flatMap(w -> w.reference.stream())
								.flatMap(r -> r.DOI.stream())
								.collect(Collectors.toList());
							mapHasReferences(doi,referencedDois,graph);
						} catch (BibliographicApiException e) {
							context.getEventBus().handleException(e);
						}
					}
				});
	}

	



}
