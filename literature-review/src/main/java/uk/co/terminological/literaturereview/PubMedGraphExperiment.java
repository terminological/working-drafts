//package uk.co.terminological.literaturereview;
//
//import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
//import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
//import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMCENTRAL_STUB;
//import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.lockNode;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapCrossRefReferences;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntriesToNode;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralCitedBy;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralReferences;
//import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubmedRelated;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Properties;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.Level;
//import org.neo4j.graphdb.Label;
//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Transaction;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import uk.co.terminological.bibliography.BibliographicApiException;
//import uk.co.terminological.bibliography.BibliographicApis;
//import uk.co.terminological.bibliography.crossref.CrossRefReference;
//import uk.co.terminological.bibliography.crossref.CrossRefSingleResult;
//import uk.co.terminological.bibliography.entrez.EntrezClient.Command;
//import uk.co.terminological.bibliography.entrez.EntrezClient.Database;
//import uk.co.terminological.bibliography.entrez.EntrezLink;
//import uk.co.terminological.bibliography.entrez.EntrezEntry;
//import uk.co.terminological.bibliography.record.IdType;
//import uk.co.terminological.pipestream.EventBus;
//import uk.co.terminological.pipestream.EventGenerator;
//import uk.co.terminological.pipestream.FluentEvents.Events;
//import uk.co.terminological.pipestream.FluentEvents.Generators;
//import uk.co.terminological.pipestream.FluentEvents.Handlers;
//import uk.co.terminological.pipestream.FluentEvents.Predicates;
//import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
//
//@Deprecated
//public class PubMedGraphExperiment {
//
//	static Logger log = LoggerFactory.getLogger(PubMedGraphExperiment.class);
//
//
//	// Event types
//	static final String PUBMED_SEARCH_RESULT = "PubMed search result";
//	static final String PUBMED_FETCH_RESULT = "PubMed fetch result";
//	static final String PMID_DOI_MAP = "PubMed to Doi";
//
//	static final String XREF_FETCH_RESULT = "CrossRef metadata entry";
//	static final String XREF_REFERENCES_FOR_DOI = "CrossRef reference list";
//
//	//Event names
//	static final String ORIGINAL_SEARCH = "Pubmed search result";
//	//static final String DOI_LIST = "DOIs";
//	//public static final String PUBMED_ENTRY_AVAILABLE = "PubMed Entry";
//
//	// Handlers & Generators
//	public static final String PUBMED_SEARCHER = "PubMed eSearch";
//	public static final String PUBMED_LINKER = "PubMed eLink";
//	public static final String PUBMED_CENTRAL_LINKER = "PubMedCentral eLink";
//
//	public static final String PUBMED_FETCHER = "PubMed eFetch";
//	public static final String XREF_LOOKUP = "Crossref lookup";
//	public static final String DOI_EXPANDER = "Resolve Dois";
//	public static final String PMID_EXPANDER = "Resolve PMIDs";
//	public static final String PMCID_EXPANDER = "Resolve PubMedCentral ids";
//
//	public static final String NEO4J_WRITER = "Neo4j pubmed node writer";
//
//	// Metadata keys
//
//	
//
//	public static void main(String args[]) throws IOException {
//
//		BasicConfigurator.configure();
//		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
//
//		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
//		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
//		Properties prop = System.getProperties();
//		prop.load(Files.newInputStream(propPath));
//
//		prop.forEach((k,v) -> prop.put(k, v.toString().replace("~", System.getProperty("user.home")))); 
//		
//		Path secretsPath = fromProperty(prop,"bibliography-secrets");
//		Path graphOptionsPath = fromProperty(prop,"graph-db-directory");
//		Path graphConfPath = fromProperty(prop,"graph-conf-file");
//		Path workingDir = fromProperty(prop,"working-directory");
//		Path outputDir = fromProperty(prop,"output-directory");
//		if (!Files.exists(graphOptionsPath)) Files.createDirectories(graphOptionsPath);
//		if (!Files.exists(workingDir)) Files.createDirectories(workingDir);
//		if (!Files.exists(outputDir)) Files.createDirectories(outputDir);
//
//
//
//		BibliographicApis biblioApi = BibliographicApis.create(secretsPath);
//		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphOptionsPath, graphConfPath);
//
//		//String search = prop.getProperty("search");
//		String broaderSearch = prop.getProperty("broader-search");
//		LocalDate earliest = LocalDate.parse(prop.getProperty("earliest"));
//		LocalDate latest = LocalDate.parse(prop.getProperty("latest"));
//
//		execute(graphApi, biblioApi, workingDir, outputDir, broaderSearch, earliest, latest);
//
//		graphApi.waitAndShutdown();
//	}
//
//	static Path fromProperty(Properties prop, String name) {
//		return Paths.get( 
//				prop.getProperty(name).replace("~", System.getProperty("user.home")));
//	}
//	
//	public static class PMIDList extends ArrayList<String> {}
//
//	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi, Path workingDir, Path outputDir, String broaderSearch, LocalDate earliest, LocalDate latest) throws IOException {
//
//		log.error("Starting graphDb build");
//		PubMedGraphSchema.setupSchema(graphApi);
//
//		try ( Transaction tx = graphApi.get().beginTx() ) {
//			lockNode = graphApi.get().createNode();
//		}
//		
//		EventBus.get()
//		.withApi(graphApi)
//		.withApi(biblioApi)
//		.withApi(new PMIDList())
//		.withEventGenerator(searchPubMed(broaderSearch, earliest, latest))
//		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(PMID_STUB))
//		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(DOI_STUB))
//		.withEventGenerator(GraphDatabaseWatcher.newLabelTrigger(EXPAND))
//		.withHandler(expandDOIStubs())
//		.withHandler(expandPMIDStubs())
//		//.withHandler(expandPMCIDStubs())
//		.withHandler(fetchPubMedEntries(earliest, latest))
//		.withHandler(findCrossRefReferencesFromNodes())
//		.withHandler(findPMCReferencesFromNodes())
//		//.withHandler(findRelatedArticlesFromNodes(broaderSearch))
//		.debugMode()
//		.execute();
//		
//		System.out.println("Press Enter key to shutdown EventBus...");
//		try
//		{
//			System.in.read();
//		}  
//		catch(Exception e)
//		{}  
//		
//		EventBus.get()
//		.sendShutdownMessage()
//		.writeExecutionGraphs(outputDir)
//		.shutdown();
//		;
//
//	}
//
//	static EventGenerator<Set<String>> searchPubMed(String search, LocalDate earliest, LocalDate latest) {
//		return Generators.generator(search, 
//				PUBMED_SEARCHER, 
//				g -> {
//					try {
//						Set<String> tmp = g.getEventBus().getApi(BibliographicApis.class).get()
//								.getEntrez()
//								.buildSearchQuery(search)
//								.betweenDates(earliest, latest)
//								.execute().get().getIds().collect(Collectors.toSet());
//								
//						g.getEventBus().logInfo("Pubmed search found: "+tmp.size()+" results");
//						//tmp = tmp.subList(0, 10);
//						return tmp;
//					} catch (BibliographicApiException e) {
//						g.getEventBus().handleException(e);
//						return Collections.emptySet();
//					}
//				},
//				name -> ORIGINAL_SEARCH, 
//				type -> PUBMED_SEARCH_RESULT);
//	}
//
//	static EventProcessor<Set<Long>> expandDOIStubs() {
//		return Handlers.eventProcessor(DOI_EXPANDER, 
//				Predicates.matchNameAndType(DOI_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
//				(event,context) -> {
//					BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
//					Set<Long> nodeIds = event.get();
//					List<String> dois = new ArrayList<>();
//					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//					try  ( Transaction tx = graph.get().beginTx() ) {
//						nodeIds.forEach(id -> {
//							Node n = graph.get().getNodeById(id);
//							Optional.ofNullable(n.getProperty("doi",null)).ifPresent(
//									doi -> dois.add(doi.toString()));
//						});
//						tx.success();
//					}
//					try {
//						while(dois.size() > 0) {
//							List<String> batchDois = dois.subList(0, Math.min(100, dois.size()));
//							Collection<String> pmids = bib.getPmcIdConv().getPMIdsByIdAndType(batchDois, IdType.DOI).values();
//							context.getEventBus().logInfo("Looked up "+batchDois.size()+" dois and found "+pmids.size()+" pubmed records");
//							context.send(
//								Events.typedEvent(pmids,type -> PUBMED_SEARCH_RESULT)
//								);
//							dois.subList(0, Math.min(100, dois.size())).clear();
//						}
//					} catch (BibliographicApiException e) {
//						context.getEventBus().handleException(e);
//					}
//				});
//	}
//	
//	static EventProcessor<Set<Long>> expandPMCIDStubs() {
//		return Handlers.eventProcessor(PMCID_EXPANDER, 
//				Predicates.matchNameAndType(PMCENTRAL_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
//				(event,context) -> {
//					BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
//					Set<Long> nodeIds = event.get();
//					List<String> pmcids = new ArrayList<>();
//					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//					try  ( Transaction tx = graph.get().beginTx() ) {
//						nodeIds.forEach(id -> {
//							Node n = graph.get().getNodeById(id);
//							Optional.ofNullable(n.getProperty("pmcid",null)).ifPresent(
//									pmcid -> pmcids.add(pmcid.toString()));
//						});
//						tx.success();
//					}
//					try {
//						while(pmcids.size() > 0) {
//							List<String> batchPmcids = pmcids.subList(0, Math.min(100, pmcids.size()));
//							Collection<String> pmids = bib.getPmcIdConv().getPMIdsByIdAndType(batchPmcids,IdType.PMCID).values();
//							context.getEventBus().logInfo("Looked up "+batchPmcids.size()+" PMCIDs and found "+pmids.size()+" pubmed records");
//							context.send(
//								Events.typedEvent(pmids,type -> PUBMED_SEARCH_RESULT)
//								);
//							pmcids.subList(0, Math.min(100, pmcids.size())).clear();
//						}
//					} catch (BibliographicApiException e) {
//						context.getEventBus().handleException(e);
//					}
//				});
//	}
//
//	static EventProcessor<Set<Long>> expandPMIDStubs() {
//		return Handlers.eventProcessor(PMID_EXPANDER, 
//				Predicates.matchNameAndType(PMID_STUB.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
//				(event,context) -> {
//					Set<Long> nodeIds = event.get();
//					List<String> pubMedIds = new ArrayList<>();
//					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//					try  ( Transaction tx = graph.get().beginTx() ) {
//						tx.acquireWriteLock(lockNode);
//						nodeIds.forEach(id -> {
//							Node n = graph.get().getNodeById(id);
//							Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
//									pmid -> pubMedIds.add(pmid.toString()));
//						});
//						tx.success();
//					}
//					context.getEventBus().logInfo("Looked up "+pubMedIds.size()+" from related pubmed records");
//					while(pubMedIds.size() > 0) {
//						context.send(
//							Events.typedEvent(
//									pubMedIds.subList(0, Math.min(100, pubMedIds.size())),
//									type -> PUBMED_SEARCH_RESULT)
//							);
//						pubMedIds.subList(0, Math.min(100, pubMedIds.size())).clear();
//					}
//				});
//	}
//
//	
//
//	static EventProcessor<Set<String>> fetchPubMedEntries(LocalDate earliest, LocalDate latest) {
//		return Handlers.eventProcessor(PUBMED_FETCHER, 
//				Predicates.matchType(PUBMED_SEARCH_RESULT), 
//				(event,context) -> {
//					try {
//						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
//						
//						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//
//						Set<EntrezEntry> entries = bib.getEntrez()
//								.getPMEntriesByPMIds(event.get());
//
//						boolean originalSearch = event.getMetadata().name().orElse("none").equals(ORIGINAL_SEARCH);
//						 
//						mapEntriesToNode(entries.stream(), graph, earliest, latest, originalSearch ? new Label[] {EXPAND} : new Label[] {});
//						
//						entries.stream().forEach(entry -> {
//							
//							context.send(
//									Events.namedTypedEvent(entry, entry.getPMID().get(), PUBMED_FETCH_RESULT)
//									);
//						});
//
//						Map<String,String> pmids2dois = new HashMap<>();
//						entries.stream().forEach(entry -> {
//							if (entry.getPMID().isPresent() && entry.getDoi().isPresent()) {
//								pmids2dois.put(entry.getPMID().get(), entry.getDoi().get());
//							}
//						});
//						context.send(Events.typedEvent(pmids2dois, type -> PMID_DOI_MAP));
//
//
//					} catch (BibliographicApiException e) {
//						context.getEventBus().handleException(e);
//					}
//				});
//	}
//
//
//	//TODO: Currently disabled as expands scope of articles. 
//	@Deprecated
//	static EventProcessor<Set<Long>> findRelatedArticlesFromNodes(String searchWithin) {
//		return Handlers.eventProcessor(PUBMED_LINKER, 
//				Predicates.matchNameAndType(EXPAND.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE),
//				
//				(event,context) -> {
//					try {
//						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
//						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//						
//						List<String> pmids = new ArrayList<>();
//						try  ( Transaction tx = graph.get().beginTx() ) {
//							tx.acquireWriteLock(lockNode);
//							event.get().forEach(id -> {
//								Node n = graph.get().getNodeById(id);
//								Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
//										pmid -> pmids.add(pmid.toString()));
//							});
//							tx.success();
//						}
//						
//						
//						
//						List<EntrezLink> tmp = bib.getEntrez()
//								.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
//								.command(Command.NEIGHBOR_SCORE)
//								.withLinkname("pubmed_pubmed")
//								.searchLinked(searchWithin)
//								.execute().stream()
//								.flatMap(o -> o.stream()).collect(Collectors.toList());
//						
//						context.getEventBus().logInfo("Found "+tmp.size()+" articles related to "+pmids.size()+" pubmed article");
//						
//						mapPubmedRelated(tmp, graph);
//
//					} catch (BibliographicApiException e) {
//						context.getEventBus().handleException(e);
//					}
//				});
//
//	}
//
//	
//	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pubmed&id=212403&cmd=neighbor&linkname=pmc_refs_pubmed
//	// provides pubmed ids for all citations if has a pmc id
//	static EventProcessor<Set<Long>> findPMCReferencesFromNodes() {
//		return Handlers.eventProcessor(PUBMED_CENTRAL_LINKER, 
//				Predicates.matchNameAndType(EXPAND.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE),
//				
//				(event,context) -> {
//					try {
//						BibliographicApis bib = context.getEventBus().getApi(BibliographicApis.class).get();
//						GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//						
//						List<String> pmids = new ArrayList<>();
//						try  ( Transaction tx = graph.get().beginTx() ) {
//							tx.acquireWriteLock(lockNode);
//							event.get().forEach(id -> {
//								Node n = graph.get().getNodeById(id);
//								Optional.ofNullable(n.getProperty("pmid",null)).ifPresent(
//										pmid -> pmids.add(pmid.toString()));
//							});
//							tx.success();
//						}
//						
//						List<EntrezLink> tmp = bib.getEntrez()
//								.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
//								.toDatabase(Database.PUBMED)
//								.command(Command.NEIGHBOR)
//								.withLinkname("pubmed_pubmed_refs")
//								//.betweenDates(earliest, LocalDate.now())
//								.execute().stream()
//								.flatMap(o -> o.stream()).collect(Collectors.toList());
//						
//						context.getEventBus().logInfo("Entrez found "+tmp.size()+" pubmed articles referenced by "+pmids.size()+" pubmed articles");
//						
//						mapPubMedCentralReferences(tmp, graph);
//						
//						List<EntrezLink> tmp2 = bib.getEntrez()
//								.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
//								.toDatabase(Database.PUBMED)
//								.command(Command.NEIGHBOR)
//								.withLinkname("pubmed_pubmed_citedin")
//								//.betweenDates(earliest, LocalDate.now())
//								.execute().stream()
//								.flatMap(o -> o.stream()).collect(Collectors.toList());
//						
//						context.getEventBus().logInfo("Entrez found "+tmp2.size()+" pubmed articles citing "+pmids.size()+" pubmed article");
//						
//						mapPubMedCentralCitedBy(tmp2, graph);
//
//					} catch (BibliographicApiException e) {
//						context.getEventBus().handleException(e);
//					}
//				});
//
//	}
//	
//	
//	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pmc&id=212403&cmd=neighbor&linkname=pmc_pmc_citedby
//	//provides pmc articles citing this pmc article	
//	
//	
//	static EventProcessor<Set<Long>> findCrossRefReferencesFromNodes() {
//		return Handlers.eventProcessor(
//				XREF_LOOKUP, 
//				Predicates.matchNameAndType(EXPAND.name(),GraphDatabaseWatcher.NEO4J_NEW_NODE), 
//				(event,context) -> {
//					BibliographicApis api = context.getEventBus().getApi(BibliographicApis.class).get();
//					GraphDatabaseApi graph = context.getEventBus().getApi(GraphDatabaseApi.class).get();
//					
//					List<String> dois = new ArrayList<>();
//					try  ( Transaction tx = graph.get().beginTx() ) {
//						tx.acquireWriteLock(lockNode);
//						event.get().forEach(id -> {
//							Node n = graph.get().getNodeById(id);
//							Optional.ofNullable(n.getProperty("doi",null)).ifPresent(
//									doi -> dois.add(doi.toString()));
//						});
//						tx.success();
//					}
//					
//					for (String doi: dois) {
//						Optional<CrossRefSingleResult> tmp = api.getCrossref().getByDoi(doi);
//						tmp.ifPresent(t ->
//								context.send(
//								Events.namedTypedEvent(t.getWork(),doi,XREF_FETCH_RESULT)
//							));
//						List<CrossRefReference> referencedDois = tmp.stream()
//							.map(t -> t.getWork())
//							.flatMap(w -> w.getReferences())
//							.collect(Collectors.toList());
//						context.getEventBus().logInfo("Crossref found "+referencedDois.size()+" articles related to: "+doi);
//						mapCrossRefReferences(doi,referencedDois,graph);
//					}
//				});
//	}
//
//	
//
//
//
//}
