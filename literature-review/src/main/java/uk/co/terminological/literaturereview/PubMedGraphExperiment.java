package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ARTICLE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.STUB;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntryToNode;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapHasReference;

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
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Label;

import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents;
import uk.co.terminological.pipestream.FluentEvents.Events;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
import uk.co.terminological.pubmedclient.BibliographicApiException;
import uk.co.terminological.pubmedclient.BibliographicApis;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;
import uk.co.terminological.pubmedclient.CrossRefResult.Work;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMedGraphExperiment {




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
	public static final String PUBMED_SEARCHER = "PubMed searcher";
	public static final String PUBMED_FETCHER = "PubMed record fetch";
	public static final String XREF_LOOKUP = "Crossref lookup";

	public static final String NEO4J_WRITER = "Neo4j pubmed node writer";

	// Metadata keys

	static Path fromProperty(Properties prop, String name) {
		return Paths.get( 
				prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}

	public static void main(String args[]) throws IOException {

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


		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		BibliographicApis biblioApi = BibliographicApis.create(secretsPath);
		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphOptionsPath);

		String search = prop.getProperty("search");

		execute(graphApi, biblioApi, workingDir, outputDir, search);
	}

	public static class PMIDList extends ArrayList<String> {}

	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi, Path workingDir, Path outputDir, String search) throws IOException {

		PubMedGraphSchema.setupSchema(graphApi);

		EventBus.get()
		.withApi(graphApi)
		.withApi(biblioApi)
		.withApi(new PMIDList())
		.withEventGenerator(searchPubMed(search))
		//.withEventGenerator(GraphDatabaseWatcher.newLabelledNodeTrigger(ARTICLE))
		//.withEventGenerator(GraphDatabaseWatcher.newLabelledNodeTrigger(STUB))

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

	static EventProcessor<List<String>> fetchPubMedEntries() {
		return Handlers.eventProcessor(PUBMED_FETCHER, 
				Predicates.matchType(PUBMED_SEARCH_RESULT), 
				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();


						PubMedEntries entries = bib.getEntrez()
								.getPMEntriesByPMIds(event.get());

						entries.stream().forEach(entry -> {
							context.send(
									//Add a depth parameter to the event 
									Events.namedTypedEvent(entry, entry.getPMID().get(), PUBMED_FETCH_RESULT)
										.put("depth",0)
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



	static EventProcessor<PubMedEntry> processDoiInCrossRef() {
		return Handlers.eventProcessor(
				XREF_LOOKUP, 
				Predicates
					.matchType(PUBMED_FETCH_RESULT)
					.and(ev -> Optional.ofNullable((Integer) ev.get("depth")).orElse(0) < 3
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

	static EventProcessor<Work> handleCrossRefReferences() {
		return Handlers.eventProcessor(
				XREF_LOOKUP, 
				Predicates.matchType(XREF_FETCH_RESULT), 
				(entry,context) -> {
					
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					Optional<String> optDoi = entry.getMetadata().name();
					List<String> referencedDois = entry.get().reference.stream()
						.flatMap(r -> r.DOI.stream())
						.collect(Collectors.toList());
					referencedDois.forEach(endDoi -> mapHasReference(optDoi.get(),endDoi,graph));
					//no reason to send further event once written to graph
					//context.send(Events.namedTypedEvent(referencedDois, optDoi.get(), XREF_REFERENCES_FOR_DOI));
				});
	}

	static EventProcessor<PubMedEntry> writeToGraph(Label... labels) {
		return Handlers.eventProcessor(
				NEO4J_WRITER, 
				Predicates.matchType(PUBMED_FETCH_RESULT), 
				(entry,context) -> {
					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
					mapEntryToNode(entry.get(), graph, labels);
				});
	}

}
