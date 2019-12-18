package uk.co.terminological.literaturereview;

import static uk.co.terminological.datatypes.StreamExceptions.tryRethrow;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
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
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import pl.edu.icm.cermine.exception.AnalysisException;
import uk.co.terminological.bibliography.BibliographicApiException;
import uk.co.terminological.bibliography.BibliographicApis;
import uk.co.terminological.bibliography.crossref.CrossRefReference;
import uk.co.terminological.bibliography.crossref.SingleResult;
import uk.co.terminological.bibliography.crossref.CrossRefWork;
import uk.co.terminological.bibliography.entrez.EntrezClient.Command;
import uk.co.terminological.bibliography.entrez.EntrezClient.Database;
import uk.co.terminological.bibliography.entrez.EntrezClient.ELinksQueryBuilder;
import uk.co.terminological.bibliography.entrez.EntrezLink;
import uk.co.terminological.bibliography.entrez.EntrezEntry;
import uk.co.terminological.bibliography.entrez.EntrezSearch;
import uk.co.terminological.bibliography.pmcidconv.PMCIDRecord;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.unpaywall.UnpaywallResult;
import uk.co.terminological.datatypes.StreamExceptions;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Labels;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Prop;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Rel;
import uk.co.terminological.nlptools.Similarity;
import uk.co.terminological.nlptools.StringCrossMapper;

public class PubMedGraphExperiment2 {

	static Logger log = LoggerFactory.getLogger(PubMedGraphExperiment2.class);


	public static void main(String args[]) throws IOException, BibliographicApiException, AnalysisException {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

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

		biblioApi = BibliographicApis.create(secretsPath);
		graphApi = GraphDatabaseApi.create(graphDbPath, graphConfPath);

		search = prop.getProperty("search");
		broaderSearch = prop.getProperty("broader-search");
		earliest = LocalDate.parse(prop.getProperty("earliest"));
		latest = LocalDate.parse(prop.getProperty("latest"));

		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile(outputDir.resolve("build.log").toString());
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.INFO);
		fa.setAppend(false);
		fa.activateOptions();

		//add appender to any Logger (here is root)
		
		org.apache.log4j.Logger.getRootLogger().addAppender(fa);
	}

	static Path fromProperty(Properties prop, String name) {
		return Paths.get(prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}

	public void execute() throws IOException, BibliographicApiException, AnalysisException {

		log.error("Starting graphDb build");
		
		//if (!graphApi.get().schema().getIndexes().iterator().hasNext()) {
		PubMedGraphSchema.setupSchema(graphApi);
		//}

		try (Transaction tx = graphApi.get().beginTx()) {
			PubMedGraphUtils.lockNode = graphApi.get().createNode();
			tx.success();
		}
		
		// get search results for broad catch all terms without any date constraints.
		// This may be a large set and should be tested to be reasonable
		EntrezSearch broadSearch = searchPubMed(this.broaderSearch).get();
		log.info("Pubmed broad search found {} articles",broadSearch.count().get());
		
		// once search is conducted use entrez history to retrieve result.
		// and write the result into the graph
		
		Set<EntrezEntry> ent = fetchPubMedEntries(broadSearch.getIds().collect(Collectors.toSet()), EXPAND);
		log.info("Of broad search pubmed found {} articles with metadata in pubmed",ent.size());
		// At this stage we have search result + metadata
		
		// Next for everything with a doi, we update metadata and expand one level using xRef
		Set<String> xrefDois = lookupDoisForUnreferenced(graphApi);
		log.info("Looking up {} articles with a doi in xRef", xrefDois.size());
		Set<String> toDois = findCrossRefReferencesFromNodes(xrefDois);
		log.info("Found {} sibling articles using xRef", toDois.size());
		
		// focussing just on those that have been added as a reference - i.e. the new ones from xref
		toDois.removeAll(xrefDois);
		log.info("Of which {} xRef articles are new", toDois.size());
		
		// link back out using pubmed this time from the set, create the links and fill in metadata from 
		// pubmed for the new articles... If there was a hit on the doi then remove it from the list of dois
		Set<String> pmidsLeftInBroaderSet = PubMedGraphUtils.lookupPMIDSForUnreferenced(graphApi);
		log.info("{} articles without references have pubmedids", pmidsLeftInBroaderSet.size());
		List<EntrezLink> links2 = findPMCReferencesFromPMIDs(pmidsLeftInBroaderSet);
		log.info("{} individual targets found in PMC (not articles though)", links2.stream().flatMap(l -> l.toId.stream()).collect(Collectors.toSet()).size());
		Set<String> pmidStubs = PubMedGraphUtils.lookupPmidStubs(graphApi);
		log.info("{} of which are new sibling articles found in pubmed",pmidStubs.size());
		Set<EntrezEntry> entries5 = fetchPubMedEntries(pmidStubs);
		//entries5.forEach(e -> e.getDoi().ifPresent(d -> toDois.remove(d)));
		
		// OK we are left with some unreferenced articles in the original broader set
		Set<String> articlesWithoutRefs = PubMedGraphUtils.lookupDoisForUnreferenced(graphApi);
		Set<String> doisInBroaderSet = PubMedGraphUtils.lookupBroadSearchDois(graphApi);
		articlesWithoutRefs.retainAll(doisInBroaderSet);
		
		log.info("{} articles in original search remain unreferences",articlesWithoutRefs.size());
		
		articlesWithoutRefs.forEach(
			StreamExceptions.ignore(
				doi -> {
					// Look these up in unpaywall and get pdfs (can do directly)
					Optional<InputStream> ois = biblioApi.getUnpaywall().getPdfByDoi(doi.toLowerCase());
					ois.ifPresent( is -> {
						
						List<String> refs = biblioApi.getPdfFetcher().extractArticleRefs(doi, is);
						log.debug("Found {} references for {}", refs.size(), doi);
						
						Set<CrossRefWork> works = refs.stream().flatMap(ref -> {
							log.debug(ref);
							return biblioApi.getCrossref().findWorkByCitationString(ref).stream();
						}).collect(Collectors.toSet());
						
						log.debug("Found {} xref entries for {} references", works.size(), refs.size());
						mapCermineReferences(doi, works, graphApi);
					});
		}));
		
		Set<String> doisMissingPMIDS = PubMedGraphUtils.lookupDoisMissingPMID(graphApi);
		Set<String> PMIDSMissingDois = PubMedGraphUtils.lookupPMIDSMissingDoi(graphApi);
		// reverse lookup dois that XRef found back but that were not linked to by pubmed
		// grab those from pubmed and update graph metadata from pubmed
		// this be a big number and need to be broken into batches?
		log.info("Mapping {} dois back to pubmed",toDois.size()); 
		tryRethrow( t -> {
			Map<String,String> moreDoi2PMIDs = biblioApi.getPmcIdConv().getPMIdsByIdAndType(doisMissingPMIDS, IdType.DOI);
			PMIDSMissingDois.addAll(moreDoi2PMIDs.values());
			Set<EntrezEntry> entries4 = fetchPubMedEntries(PMIDSMissingDois);
			// entries4.forEach(e -> e.getDoi().ifPresent(d -> toDois.remove(d)));
			log.info("Found pmids for {} entries which were previously missing them",entries4.size());
		});
		
		// there are now probably a lot of articles for which we could get basic metadata from xref
		// key bit we need is cited by.
		Set<String> xrefDois2 = PubMedGraphUtils.lookupDoisForUnknownCitedBy(graphApi);
		Set<String> toDois2 = updateMetadataFromCrossRef(xrefDois2);
		
		log.info("Updated {} article metadata from xRef which were previously missing",toDois2.size());
		
		// there may be a few straggles for which we cannot get metadata from xref
		// highly unlikely that we can get them from unpaywall but give it a go. 
		Set<String> doiStub = PubMedGraphUtils.lookupDoiStubs(graphApi);
		//log.info("Looking up {} dois with no metadata on Unpaywall",doiStub.size());
		Set<String> unpaywallSources = updateMetadataFromUnpaywall(doiStub);
		log.info("Updated {} article metadata from Unpaywall which were previously missing",unpaywallSources.size());

		// the basic broad search articles - find pdf links.
		//TODO: should this be determined in response to the pagerank? 
		Set<String> loadedDois = PubMedGraphUtils.lookupBroadSearchDois(graphApi);
		//log.info("finding open access pdf links for {} dois",loadedDois.size());
		Set<String> identifyPdf = updatePdfLinksFromUnpaywall(loadedDois);
		log.info("found open access pdf links for {} dois",identifyPdf.size());
		
		StringCrossMapper mapper = new StringCrossMapper(Arrays.asList("University","Institute","Department", "Research","of","at","is","a","for", "Dept"));
		log.info("loading affiliations from graph");
		
		try (Transaction tx = graphApi.get().beginTx()) {
			
			graphApi.get().findNodes(Labels.AFFILIATION).stream().forEach( //.limit(30).forEach(
				n -> {
					String affil = n.getProperty(Prop.ORGANISATION_NAME).toString();
					mapper.addSource(Long.toString(n.getId()),affil.toString()); 
					mapper.addTarget(Long.toString(n.getId()),affil.toString());
			});
			
			log.info(mapper.summaryStats());
			mapper.getAllMatchesBySimilarity(0.9D, d -> d.termsByTfIdf(), Similarity::getCosineDifference).forEach(triple -> {
				if (!triple.getFirst().equals(triple.getSecond())) {
					Node in = graphApi.get().getNodeById(Long.parseLong(triple.getFirst().getIdentifier()));
					Node out = graphApi.get().getNodeById(Long.parseLong(triple.getSecond().getIdentifier()));
					Relationship r = in.createRelationshipTo(out, Rel.SIMILAR_TO);
					r.setProperty(Prop.SCORE, triple.getThird());
				}
				tx.success();
			});
			
		}
		
		Yaml yaml = new Yaml();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("cypherQuery.yaml");
		Map<String, Object> obj = yaml.load(inputStream);
		
		@SuppressWarnings("unchecked")
		List<Map<String,String>> queries = (List<Map<String, String>>) obj.get("build");
		queries.forEach(map -> {
			map.forEach((k,v) -> {
				log.info("Executing: "+k);
				try (Transaction tx = graphApi.get().beginTx()) {
					org.neo4j.graphdb.Result r = graphApi.get().execute(v);
					QueryStatistics q = r.getQueryStatistics();
					log.info("nodes: added {},removed {}; relationships: added {},removed {}; properties: added {}",
							q.getNodesCreated(),
							q.getNodesDeleted(),
							q.getRelationshipsCreated(),
							q.getRelationshipsDeleted(),
							q.getPropertiesSet()
							);
					tx.success();
				}
			});
		});
		
		
		
	}

	

	Optional<EntrezSearch> searchPubMed(String search) {
		try {
			Optional<EntrezSearch> tmp = biblioApi.getEntrez()
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

	/*Optional<Search> fullSearchPubMed(String search) {
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
	}*/

	List<PMCIDRecord> lookupIdMapping(List<String> ids, IdType ofType) {
		List<PMCIDRecord> out = new ArrayList<>();

		try {
			while(ids.size() > 0) {
				List<String> batchDois = ids.subList(0, Math.min(100, ids.size()));
				Set<PMCIDRecord> pmids = biblioApi.getPmcIdConv()
						.getMapping(batchDois, ofType);
				log.debug("Looked up "+batchDois.size()+" "+ofType.name()+" and found "+pmids.size()+" linked records");
				out.addAll(pmids);
				ids.subList(0, Math.min(100, ids.size())).clear();
			}
		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return out;

	}

	Set<EntrezEntry> fetchPubMedEntries(Collection<String> pmids, Label... labels) {
		Set<EntrezEntry> entriesOut = new HashSet<>();
		List<String> deferred = new ArrayList<>(pmids);
		while (!deferred.isEmpty()) {
			try {
				int size = 300 > deferred.size()? deferred.size(): 300;
				Set<EntrezEntry> entries = biblioApi.getEntrez().getPMEntriesByPMIds(deferred.subList(0, size));
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
			List<EntrezLink> tmp = biblioApi.getEntrez()
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

	
	List<EntrezLink> findPMCReferencesFromPMIDs(Collection<String> pmids) {
		return findPMCReferences(biblioApi.getEntrez().buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED));
	}

	/*List<Link> findPMCReferencesFromSearch(Search search) {
		return findPMCReferences(biblioApi.getEntrez().buildLinksQueryForSearchResult(search, Database.PUBMED));
	}*/
	
	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pubmed&id=212403&cmd=neighbor&linkname=pmc_refs_pubmed
	// provides pubmed ids for all citations if has a pmc id
	List<EntrezLink> findPMCReferences(ELinksQueryBuilder elqb) {
		try {

			List<EntrezLink> tmp = elqb
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

	List<EntrezLink> findPMCCitedByPMIDs(Collection<String> pmids) {
		return findPMCCitedBy(biblioApi.getEntrez().buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED));
	}
	
	List<EntrezLink> findPMCCitedBy(ELinksQueryBuilder elqb) {
		try {

			List<EntrezLink> tmp = elqb
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

	
	
	
	/**
	 * gets a record for each doi, updates the metadata a matching article in the graph or creates one
	 * creates a stub record with basic metadata for each of the citations (or matches an existing record)
	 * creates the links between cited and citing
	 * @param dois
	 * @return a set of dois representing the references
	 */
	Set<String> findCrossRefReferencesFromNodes(Set<String> dois) {
		Set<String> outDois = new HashSet<>();
		for (String doi: dois) {
			Optional<SingleResult> tmp = biblioApi.getCrossref().getByDoi(doi);
			tmp.ifPresent(t -> updateCrossRefMetadata(t.getWork(),graphApi));
			List<CrossRefReference> referencedDois = tmp.stream()
					.map(t -> t.getWork())
					.flatMap(w -> w.getReferences())
					.collect(Collectors.toList());
			log.debug("Crossref found "+referencedDois.size()+" articles related to: "+doi);
			mapCrossRefReferences(doi,referencedDois,graphApi);
			outDois.addAll(referencedDois.stream().flatMap(c -> c.getIdentifier().stream()).map(s -> s.toLowerCase()).collect(Collectors.toSet()));
		}
		return outDois;
	}

	Set<String> updateMetadataFromCrossRef(Set<String> dois) {
		Set<String> outDois = new HashSet<>();
		for (String doi: dois) {
			Optional<SingleResult> tmp = biblioApi.getCrossref().getByDoi(doi);
			tmp.ifPresent(t -> {
					Optional<String> out = updateCrossRefMetadata(t.getWork(),graphApi);
					out.ifPresent(o->outDois.add(o.toLowerCase()));
					});
		}
		return outDois;
	}
	
	Set<String> updateMetadataFromUnpaywall(Set<String> dois) {
		Set<String> out = new HashSet<String>();
		for (String doi: dois) {
			Optional<UnpaywallResult> res = biblioApi.getUnpaywall().getUnpaywallByDoi(doi.toLowerCase());
			res.ifPresent(r -> {
				log.debug("found unpaywall entry for: "+doi);
				updateUnpaywallMetadata(r, graphApi).ifPresent(d -> out.add(d.toLowerCase()));
			});
		}
		return out;
	}
	
	Set<String> updatePdfLinksFromUnpaywall(Set<String> dois) {
		Set<String> out = new HashSet<String>();
		for (String doi: dois) {
			Optional<UnpaywallResult> res = biblioApi.getUnpaywall().getUnpaywallByDoi(doi.toLowerCase());
			res.ifPresent(r -> {
				log.debug("found unpaywall entry for: "+doi);
				updatePdfLink(r, graphApi).ifPresent(d -> out.add(d.toLowerCase()));
			});
		}
		return out;
	}
	
	
	
}
