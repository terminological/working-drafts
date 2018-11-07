package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMed2Neo4jUtils.mapEntryToNode;
import static uk.co.terminological.literaturereview.PubMed2Neo4jUtils.setupSchema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
import uk.co.terminological.pubmedclient.BibliographicApiException;
import uk.co.terminological.pubmedclient.BibliographicApis;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;

public class PubMed2Neo4jExperiment {
	
	
	
	
	// Event types
	public static final String PUBMED_SEARCH_RESULT = "Pubmed search result";
	public static final String NEO4J_NEW_NODE = "Neo4j node created";
	
	// Event names
	
	// Handlers & Generators
	public static final String PUBMED_SEARCHER = "Pubmed searcher";
	public static final String PUBMED_FETCHER = "Pubmed record fetch";
	public static final String NEO4J_ARTICLE_FINDER = "Neo4j new article finder";
	public static final String NEO4J_NODE_WATCHER = "Neo4j node watcher";
	
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
	
	public static enum Status {}
	
	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi, Path workingDir, Path outputDir, String search) throws IOException {
		
		setupSchema(graphApi);
		
		EventBus.get()
			.withApi(graphApi)
			.withApi(biblioApi)
			//.withApi(new StatusRecord<Status>())
			.withEventGenerator(pubMedResults(search,"machine learning"))
			
			;
		
	}

	static EventGenerator<List<String>> pubMedResults(String search, String searchName) {
		return Generators.generator(searchName, 
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
				name -> searchName, 
				type -> PUBMED_SEARCH_RESULT);
	}
	
	static EventProcessor<List<String>> getEntriesFromIds() {
		return Handlers.eventProcessor(PUBMED_FETCHER, 
				Predicates.matchType(PUBMED_SEARCH_RESULT), 
				(event,context) -> {
					try {
						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
						
						PubMedEntries entries = bib.getEntrez()
							.getPMEntriesByPMIds(event.get());
						
						entries.stream().forEach(
								entry -> mapEntryToNode(entry, graph, PubMed2Neo4jUtils.SEARCH_RESULT) 
						);
												
						entries.stream().flatMap(entry -> entry.getDoi().stream());
						
						
					} catch (BibliographicApiException e) {
						context.getEventBus().handleException(e);
					}
				});
	}
	
	static EventGenerator<Long> newlabelledNodeTrigger(Label label) {
		return GraphDatabaseWatcher.create(label.name(), NEO4J_NODE_WATCHER, 
				(txData, context) -> {
					txData.createdNodes().forEach( node -> {
						if (node.hasLabel(label)) {
							context.send(
								FluentEvents.Events.namedTypedEvent(node.getId(), label.name(), NEO4J_NEW_NODE)	
							);
						}
					});
				});
	}
	
	
}
