package uk.co.terminological.literaturereview;

import static uk.co.terminological.datatypes.StreamExceptions.tryRethrow;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ARTICLE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ORIGINAL_SEARCH;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Props.PMID;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.lookupDoisForUnreferenced;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapCermineReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapCrossRefReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntriesToNode;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralCitedBy;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubmedRelated;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.updateCrossRefMetadata;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.updatePdfLink;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.updateUnpaywallMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.bibref.model.BibEntryType;
import pl.edu.icm.cermine.exception.AnalysisException;
import uk.co.terminological.datatypes.StreamExceptions;
import uk.co.terminological.pubmedclient.BibliographicApiException;
import uk.co.terminological.pubmedclient.BibliographicApis;
import uk.co.terminological.pubmedclient.CrossRefResult.Reference;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;
import uk.co.terminological.pubmedclient.CrossRefResult.Work;
import uk.co.terminological.pubmedclient.EntrezClient.Command;
import uk.co.terminological.pubmedclient.EntrezClient.Database;
import uk.co.terminological.pubmedclient.EntrezClient.ELinksQueryBuilder;
import  uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;
import uk.co.terminological.pubmedclient.EntrezResult.Search;
import uk.co.terminological.pubmedclient.IdConverterClient.IdType;
import uk.co.terminological.pubmedclient.IdConverterClient.Record;
import uk.co.terminological.pubmedclient.UnpaywallClient.Result;

public class PubMedGraphExperiment2 {

	static Logger log = LoggerFactory.getLogger(PubMedGraphExperiment2.class);


	public static void main(String args[]) throws IOException, BibliographicApiException, AnalysisException {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);

		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(propPath));

		PubMedGraphExperiment2 tmp = new PubMedGraphExperiment2(prop);
		tmp.execute();
		tmp.graphApi.waitAndShutdown();
	}

	private Path secretsPath;
	private Path graphDbPath;
	private Path graphConfPath;
	private Path workingDir;
	private Path outputDir;
	private BibliographicApis biblioApi;
	private GraphDatabaseApi graphApi;
	private String search;
	private String broaderSearch;
	private LocalDate earliest;
	private LocalDate latest;


	public PubMedGraphExperiment2(Properties prop) throws IOException {

		prop.forEach((k,v) -> prop.put(k, v.toString().replace("~", System.getProperty("user.home")))); 
		secretsPath = fromProperty(prop,"bibliography-secrets");
		graphDbPath = fromProperty(prop,"graph-db-directory");
		graphConfPath = fromProperty(prop,"graph-conf-file");
		workingDir = fromProperty(prop,"working-directory");
		outputDir = fromProperty(prop,"output-directory");
		if (!Files.exists(graphDbPath)) Files.createDirectories(graphDbPath.resolve("certificates"));
		if (!Files.exists(workingDir)) Files.createDirectories(workingDir);
		if (!Files.exists(outputDir)) Files.createDirectories(outputDir);

		biblioApi = BibliographicApis.create(secretsPath, workingDir);
		graphApi = GraphDatabaseApi.create(graphDbPath, graphConfPath);

		search = prop.getProperty("search");
		broaderSearch = prop.getProperty("broader-search");
		earliest = LocalDate.parse(prop.getProperty("earliest"));
		latest = LocalDate.parse(prop.getProperty("latest"));

	}

	static Path fromProperty(Properties prop, String name) {
		return Paths.get(prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}

	public void execute() throws IOException, BibliographicApiException, AnalysisException {

		log.error("Starting graphDb build");
		
		//if (!graphApi.get().schema().getIndexes().iterator().hasNext()) {
			PubMedGraphSchema.setupSchema(graphApi);
		//}

		//TODO: Main loop
			
		try ( Transaction tx = graphApi.get().beginTx() ) {
			PubMedGraphUtils.lockNode = graphApi.get().createNode();
			tx.success();
		}
			
		
		//TODO: some problem with nodeId 1 - How Cognitive Machines Can Augment Medical Imaging
		
		// get search results for broad catch all terms without any date constraints.
		// This may be a large set and shoudl be tested to be reasonable
		Search broadSearch = fullSearchPubMed(this.broaderSearch).get();
		log.info("Pubmed broad search found to {} articles with metadata",broadSearch.count().get());
		
		// once search is conducted use entrez history to retrieve result.
		// and write the result into the graph
		// TODO: what fields actually need to be written into graph?
		// TODO: best to cache the results of this maybe, or at least save the answer for downstream?
		
		Path pubmedXmlCache = workingDir.resolve("xml");
		/*PubMedEntries ent = broadSearch.getStoredResult(biblioApi.getEntrez()).get();
		log.info("Pubmed broad search found to {} articles with metadata",ent.stream().count());
		
		mapEntriesToNode(ent.stream(), graphApi, earliest, latest, EXPAND);
		
		tryRethrow(pubmedXmlCache, t -> Files.createDirectories(t));
		ent.stream().forEach(
				logWarn(entry -> {
					Path tmp2 = pubmedXmlCache.resolve(entry.getPMID().orElseThrow(() -> new IOException("No pmid")));
					entry.getRaw().write(Files.newOutputStream(tmp2));
		}));*/
		
		Set<PubMedEntry> ent = fetchPubMedEntries(pubmedXmlCache, broadSearch.getIds().collect(Collectors.toSet()), EXPAND);
		log.info("Pubmed broad search found to {} articles with metadata",ent.size());
		
		// get narrow search result - date constrained specific search.
		// the intersection of this set and the borader set will be tagged to make finding them easier.
		Optional<Search> narrowSearchIds = searchPubMed(this.search);
		Set<String> pmids = narrowSearchIds.get().getIds().collect(Collectors.toSet());
		log.info("Pubmed narrow search refer to {} articles",pmids.size());
		PubMedGraphUtils.addLabelsByIds(ARTICLE, PMID, pmids, ORIGINAL_SEARCH, graphApi);
		
		// get all the links for the broad search using entrez history
		// and write them into database. crating stubs if required
		// work out what pmids we already have written in graph from the broader search and which we need to get.
		List<Link> links = findPMCCitedByPMIDs(broadSearch.getIds().collect(Collectors.toList()));
		Set<String> ancestorPMIDs = links.stream().map(l -> l.toId.get()).collect(Collectors.toSet());
		
		Set<String> broadSearchPlusAncestorPMIDs =broadSearch.getIds().collect(Collectors.toSet());
		//TODO: broadSearchPlusAncestorPMIDs.addAll(ancestorPMIDs);
		
		List<Link> links2 = findPMCReferencesFromPMIDs(broadSearchPlusAncestorPMIDs);
		
		Set<String> toPMIDs = links2.stream().map(l -> l.toId.get()).collect(Collectors.toSet());
		//toPMIDs.addAll(links2.stream().map(l -> l.fromId).collect(Collectors.toSet()));
		
		log.info("Pubmed broad search refer to {} articles",toPMIDs.size());
		Set<String> loadedPMIDs = ent.stream().flatMap(e -> e.getPMID().stream()).collect(Collectors.toSet());
		toPMIDs.removeAll(loadedPMIDs);
		log.info("Of which {} are articles outside of broad search",toPMIDs.size());
		
		
		// fetch all the entries that were outside broader search but pointed to by pmid citations
		// write these in as stubs.
		
		
		
		Set<PubMedEntry> entries2 = fetchPubMedEntries(pubmedXmlCache,toPMIDs);
		
		
		
		
		// now for DOIs...
		// collect all DOIs in the graph so far. This could be done by a query (which may give more accurate
		Set<String> broadSearchDois = ent.stream().flatMap(e -> e.getDoi().stream()).map(s-> s.toLowerCase()).collect(Collectors.toSet());
		log.info("Pubmed broad search include {} articles with a doi",broadSearchDois.size());
		Set<String> xrefDois = lookupDoisForUnreferenced(graphApi);
		log.info("Of which {} articles have no references yet and we can look up in Xref",xrefDois.size());
		//TODO: could lookup those without a doi in id cross reference - maybe the same data though.
		Set<String> loadedDois = new HashSet<>(broadSearchDois);
		loadedDois.addAll(entries2.stream().flatMap(e -> e.getDoi().stream()).map(s-> s.toLowerCase()).collect(Collectors.toSet()));
		log.info("With referenced articles there are {} articles with a doi",loadedDois.size()); 
		
		
		// fetch all doi cross references for broader search, load into graph.
		// and find all resulting dois that we do not already know about.
		// TODO: chance that mapping api does not know about a PMID->DOI mapping that we do already know about via pubmed - does this matter?
		Path xrefJsonCache = workingDir.resolve("xref");
		Set<String> toDois = findCrossRefReferencesFromNodes(xrefJsonCache,xrefDois);
		log.info("Found {} dois, referred to by {} articles in broad search with doi", toDois.size(), broadSearchDois.size());
		toDois.removeAll(loadedDois);
		log.info("Of which {} are not yet known in the graph", toDois.size());
		
		// Find out which broadSearch nodes have dois and no references (by query)
		Set<String> pdfDois = lookupDoisForUnreferenced(graphApi); 
		log.info("Found {} articles with no references", pdfDois.size());
		
		Path unpaywallCache = workingDir.resolve("pdf");
		
		pdfDois.forEach(
				StreamExceptions.ignore(
						doi -> {
							// Look these up in unpaywall and get pdfs (can do directly)
							InputStream is = biblioApi.getUnpaywall().getPdfByDoi(doi.toLowerCase(), unpaywallCache);
							log.info("Found pdf for {}", doi);
							ContentExtractor extractor = new ContentExtractor();
							extractor.setPDF(is);
							//Use cermine to get references
							List<BibEntry> refs = extractor.getReferences();
							log.info("Found {} references for {}", refs.size(), doi);
							Set<Work> works = refs.stream()
									.filter(ref -> Arrays.asList(
											BibEntryType.ARTICLE,
											BibEntryType.INPROCEEDINGS,
											BibEntryType.PROCEEDINGS
											).contains(ref.getType()))
									.flatMap(ref -> {
								//Use xref to get a doi for citations string.
								log.info(ref.getText());
								
								return biblioApi.getCrossref().findWorkByCitationString(ref.getText()).stream();
							}).collect(Collectors.toSet());
							
							log.info("Found {} xref entries for {} references", works.size(), refs.size());
							mapCermineReferences(doi, works, graphApi);
		}));
		
		
		
		
		// reverse lookup unknown dois that XRef found but are not already in the graph
		// grab those from pubmed and update graph metadata
		// TODO: will this be a massive number and need to be broken into batches?
		log.info("Mapping {} dois back to pubmed",toDois.size()); 
		tryRethrow( t -> {
			Set<String> morePMIDs = biblioApi.getPmcIdConv().getPMIdsByIdAndType(toDois, IdType.DOI);
			Set<PubMedEntry> entries4 = fetchPubMedEntries(pubmedXmlCache,morePMIDs);
			entries4.forEach(e -> e.getDoi().ifPresent(f -> loadedDois.add(f.toLowerCase())));
			log.info("Found additional {} pubmed entries",entries4.size());
		}); 
		
		// There are some DOIs that will neither have been found by original pubmed searched or the pubmed id converter.
		// We look them up in XRef
		// TODO: Cache metadata entry in case
		toDois.removeAll(loadedDois);
		log.info("Looking up {} dois with metadata on Xref",toDois.size());
		Set<String> xrefSourced = updateMetadataFromCrossRef(xrefJsonCache, toDois);
		loadedDois.addAll(xrefSourced);
		toDois.removeAll(xrefSourced);
		
		
		// TODO: grab pdfs for broader search nodes (with EXPAND label) using unpaywall and broadSearchDois.
		// TODO: Query graph for broader search nodes (labelled with EXPAND) that do not have any citations - these could be 
		// TODO: Grab the pdfs for these and resolve the references from the original citations.... yikes.
		
		log.info("Looking up {} dois with no metadata on Unpaywall",toDois.size());
		Set<String> unpaywallSources = updateMetadataFromUnpaywall(unpaywallCache, toDois);
		toDois.removeAll(unpaywallSources);
		loadedDois.addAll(unpaywallSources);
		log.info("Leaving {} dois with no metadata",toDois.size());

		
		log.info("finding open access pdf links for {} dois",loadedDois.size());
		Set<String> identifyPdf = updatePdfLinksFromUnpaywall(unpaywallCache, loadedDois);
		log.info("found open access pdf links for {} dois",identifyPdf.size());
		
	}

	

	Optional<Search> searchPubMed(String search) {
		try {
			Optional<Search> tmp = biblioApi.getEntrez()
					.buildSearchQuery(search)
					.betweenDates(earliest, latest)
					.execute();
			log.info("Pubmed search found: "+tmp.flatMap(o -> o.count()).orElse(0)+" results");
			return tmp;
		} catch (BibliographicApiException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	Optional<Search> fullSearchPubMed(String search) {
		try {
			Optional<Search> tmp = biblioApi.getEntrez()
					.buildSearchQuery(search)
					.execute();
			log.debug("Pubmed search found: "+tmp.flatMap(o -> o.count()).orElse(0)+" results");
			return tmp;
		} catch (BibliographicApiException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	List<Record> lookupIdMapping(List<String> ids, IdType ofType) {
		List<Record> out = new ArrayList<>();

		try {
			while(ids.size() > 0) {
				List<String> batchDois = ids.subList(0, Math.min(100, ids.size()));
				List<Record> pmids = biblioApi.getPmcIdConv()
						.getConverterForIdsAndType(batchDois, ofType)
						.records
						;
				log.debug("Looked up "+batchDois.size()+" "+ofType.name()+" and found "+pmids.size()+" linked records");
				out.addAll(pmids);
				ids.subList(0, Math.min(100, ids.size())).clear();
			}
		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return out;

	}

	Set<PubMedEntry> fetchPubMedEntries(Path cacheDir, Collection<String> pmids, Label... labels) {
		Set<PubMedEntry> entriesOut = new HashSet<>();
		List<String> deferred = new ArrayList<>(pmids);
		while (!deferred.isEmpty()) {
			try {
				int size = 7000>deferred.size()? deferred.size(): 7000;
				Set<PubMedEntry> entries = biblioApi.getEntrez().getPMEntriesByPMIds(deferred.subList(0, size),cacheDir);
				mapEntriesToNode(entries.stream(), graphApi, earliest, latest, labels);
				log.info("retrieved {} articles referred to in broad search",entries.stream().count());
				deferred.subList(0, size).clear();
				entriesOut.addAll(entries);
			} catch (BibliographicApiException e) {
				e.printStackTrace();
			}
		}
		return entriesOut;
		
	}


	List<Relationship> findRelatedArticlesFromPMIDs(List<String> pmids, String searchWithin) {
		try {
			List<Link> tmp = biblioApi.getEntrez()
					.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
					.command(Command.NEIGHBOR_SCORE)
					.withLinkname("pubmed_pubmed")
					.searchLinked(searchWithin)
					.execute().stream()
					.flatMap(o -> o.stream()).collect(Collectors.toList());

			log.info("Found "+tmp.size()+" articles related to "+pmids.size()+" pubmed article");

			return mapPubmedRelated(tmp, graphApi);

		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	
	List<Link> findPMCReferencesFromPMIDs(Collection<String> pmids) {
		return findPMCReferences(biblioApi.getEntrez().buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED));
	}

	/*List<Link> findPMCReferencesFromSearch(Search search) {
		return findPMCReferences(biblioApi.getEntrez().buildLinksQueryForSearchResult(search, Database.PUBMED));
	}*/
	
	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pubmed&id=212403&cmd=neighbor&linkname=pmc_refs_pubmed
	// provides pubmed ids for all citations if has a pmc id
	List<Link> findPMCReferences(ELinksQueryBuilder elqb) {
		try {

			List<Link> tmp = elqb
					.toDatabase(Database.PUBMED)
					.command(Command.NEIGHBOR)
					.withLinkname("pubmed_pubmed_refs")
					.execute().get().getLinks();

			log.info("Entrez found "+tmp.size()+" pubmed articles referenced by pubmed articles");

			mapPubMedCentralReferences(tmp, graphApi);

			return tmp;

		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	List<Link> findPMCCitedByPMIDs(Collection<String> pmids) {
		return findPMCCitedBy(biblioApi.getEntrez().buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED));
	}
	
	List<Link> findPMCCitedBy(ELinksQueryBuilder elqb) {
		try {

			List<Link> tmp = elqb
					.toDatabase(Database.PUBMED)
					.command(Command.NEIGHBOR)
					.withLinkname("pubmed_pubmed_citedin")
					.execute().get().getLinks();

			log.info("Entrez found "+tmp.size()+" pubmed articles citing pubmed articles");

			mapPubMedCentralCitedBy(tmp, graphApi);

			return tmp;

		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();

	}

	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pmc&id=212403&cmd=neighbor&linkname=pmc_pmc_citedby
	//provides pmc articles citing this pmc article	

	
	
	

	Set<String> findCrossRefReferencesFromNodes(Path xrefCacheDir, Set<String> dois) {
		Set<String> outDois = new HashSet<>();
		for (String doi: dois) {
			try {
				Optional<SingleResult> tmp = biblioApi.getCrossref().getByDoi(doi);
				List<Reference> referencedDois = tmp.stream()
						.flatMap(t -> t.work.stream())
						.flatMap(w -> w.reference.stream())
						.collect(Collectors.toList());
				log.debug("Crossref found "+referencedDois.size()+" articles related to: "+doi);
				mapCrossRefReferences(doi,referencedDois,graphApi);
				outDois.addAll(referencedDois.stream().flatMap(c -> c.DOI.stream()).map(s -> s.toLowerCase()).collect(Collectors.toSet()));
			} catch (BibliographicApiException e) {
				e.printStackTrace();
			}
		}
		return outDois;
	}

	Set<String> updateMetadataFromCrossRef(Path xrefCacheDir, Set<String> dois) {
		Set<String> outDois = new HashSet<>();
		for (String doi: dois) {
			try {
				Optional<SingleResult> tmp = biblioApi.getCrossref().getByDoi(doi);
				tmp.ifPresent(t -> {
					t.work.ifPresent(w -> {
						Optional<String> out = updateCrossRefMetadata(w,graphApi);
						out.ifPresent(o->outDois.add(o.toLowerCase()));
						});
				});
			} catch (BibliographicApiException e) {
				//e.printStackTrace();
			}
		}
		return outDois;
	}
	
	Set<String> updateMetadataFromUnpaywall(Path unpaywallCache, Set<String> dois) {
		Set<String> out = new HashSet<String>();
		for (String doi: dois) {
			try {
				Result r = biblioApi.getUnpaywall().getUnpaywallByDoi(doi.toLowerCase(), unpaywallCache);
				updateUnpaywallMetadata(r, graphApi).ifPresent(d -> out.add(d.toLowerCase()));
			} catch (BibliographicApiException e) {
				//e.printStackTrace();
			} 
		}
		return out;
	}
	
	Set<String> updatePdfLinksFromUnpaywall(Path unpaywallCache, Set<String> dois) {
		Set<String> out = new HashSet<String>();
		for (String doi: dois) {
			try {
				
				Result r = biblioApi.getUnpaywall().getUnpaywallByDoi(doi.toLowerCase(), unpaywallCache);
				log.debug("found unpaywall entry for: "+doi);
				updatePdfLink(r, graphApi).ifPresent(d -> out.add(d.toLowerCase()));;
			} catch (BibliographicApiException e) {
				log.debug("did not find unpaywall entry for: "+doi);
				//log.debug(e.getMessage());
			} 
		}
		return out;
	}
	
}
