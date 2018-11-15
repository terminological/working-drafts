package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphUtils.lockNode;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMCENTRAL_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapCrossRefReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntriesToNode;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralCitedBy;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubmedRelated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
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
import uk.co.terminological.pubmedclient.EntrezClient.Command;
import uk.co.terminological.pubmedclient.EntrezClient.Database;
import  uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.IdConverterClient.IdType;

public class PubMedGraphExperiment2 {

	static Logger log = LoggerFactory.getLogger(PubMedGraphExperiment2.class);


	public static void main(String args[]) throws IOException {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.DEBUG);

		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(propPath));

		new PubMedGraphExperiment2(prop).execute();
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

	}

	static Path fromProperty(Properties prop, String name) {
		return Paths.get(prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}

	public void execute() throws IOException {

		log.error("Starting graphDb build");
		PubMedGraphSchema.setupSchema(graphApi);

		try ( Transaction tx = graphApi.get().beginTx() ) {
			lockNode = graphApi.get().createNode();
		}

		//TODO: Main loop


		System.out.println("Press Enter key to shutdown EventBus...");
		try
		{
			System.in.read();
		}  
		catch(Exception e)
		{}  

		graphApi.waitAndShutdown();

	}

	List<String> searchPubMed(String search) {
		try {
			List<String> tmp = biblioApi.getEntrez()
					.buildSearchQuery(search)
					.betweenDates(earliest, latest)
					.execute().get().getIds();

			log.info("Pubmed search found: "+tmp.size()+" results");
			return tmp;
		} catch (BibliographicApiException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}


	List<String> expandDOIStubs(List<String> dois) {
		List<String> out = new ArrayList<>();

		try {
			while(dois.size() > 0) {
				List<String> batchDois = dois.subList(0, Math.min(100, dois.size()));
				List<String> pmids = biblioApi.getPmcIdConv().getPMIdsByIdAndType(batchDois, IdType.DOI);
				log.info("Looked up "+batchDois.size()+" dois and found "+pmids.size()+" pubmed records");
				out.addAll(pmids);
				dois.subList(0, Math.min(100, dois.size())).clear();
			}
		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return out;

	}

	List<String> expandPMCIDStubs(List<String> pmcids) {
		List<String> out = new ArrayList<String>();
		try {
			while(pmcids.size() > 0) {
				List<String> batchPmcids = pmcids.subList(0, Math.min(100, pmcids.size()));
				List<String> pmids = biblioApi.getPmcIdConv().getPMIdsByIdAndType(batchPmcids,IdType.PMCID);
				log.info("Looked up "+batchPmcids.size()+" PMCIDs and found "+pmids.size()+" pubmed records");
				out.addAll(pmids);
				pmcids.subList(0, Math.min(100, pmcids.size())).clear();
			}
		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return out;
	}





	List<Node> fetchPubMedEntries(List<String> pmids, boolean originalSearch) {
		List<Node> out = new ArrayList<>();
		try {
			PubMedEntries entries = biblioApi.getEntrez().getPMEntriesByPMIds(pmids);
			out = mapEntriesToNode(entries, graphApi, earliest, latest, originalSearch);

			Map<String,String> pmids2dois = new HashMap<>();
			entries.stream().forEach(entry -> {
				if (entry.getPMID().isPresent() && entry.getDoi().isPresent()) {
					pmids2dois.put(entry.getPMID().get(), entry.getDoi().get());
				}
			});


		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return out;
	}


	List<Relationship> findRelatedArticlesFromPMIDS(List<String> pmids, String searchWithin) {
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


	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pubmed&id=212403&cmd=neighbor&linkname=pmc_refs_pubmed
	// provides pubmed ids for all citations if has a pmc id
	List<Link> findPMCReferencesFromNodes(List<String> pmids) {
		try {

			List<Link> tmp = biblioApi.getEntrez()
					.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
					.toDatabase(Database.PUBMED)
					.command(Command.NEIGHBOR)
					.withLinkname("pubmed_pubmed_refs")
					//.betweenDates(earliest, LocalDate.now())
					.execute().stream()
					.flatMap(o -> o.stream()).collect(Collectors.toList());

			log.info("Entrez found "+tmp.size()+" pubmed articles referenced by "+pmids.size()+" pubmed articles");

			mapPubMedCentralReferences(tmp, graphApi);

			List<Link> tmp2 = biblioApi.getEntrez()
					.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
					.toDatabase(Database.PUBMED)
					.command(Command.NEIGHBOR)
					.withLinkname("pubmed_pubmed_citedin")
					//.betweenDates(earliest, LocalDate.now())
					.execute().stream()
					.flatMap(o -> o.stream()).collect(Collectors.toList());

			log.info("Entrez found "+tmp2.size()+" pubmed articles citing "+pmids.size()+" pubmed article");

			mapPubMedCentralCitedBy(tmp2, graphApi);

			tmp.addAll(tmp2);
			return tmp;

		} catch (BibliographicApiException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();

	}


	//https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pmc&db=pmc&id=212403&cmd=neighbor&linkname=pmc_pmc_citedby
	//provides pmc articles citing this pmc article	


	List<Relationship> findCrossRefReferencesFromNodes(List<String> dois) {
		for (String doi: dois) {
			try {
				Optional<SingleResult> tmp = biblioApi.getCrossref().getByDoi(doi);
				List<String> referencedDois = tmp.stream()
						.flatMap(t -> t.work.stream())
						.flatMap(w -> w.reference.stream())
						.flatMap(r -> r.DOI.stream())
						.collect(Collectors.toList());
				log.info("Crossref found "+referencedDois.size()+" articles related to: "+doi);
				return mapCrossRefReferences(doi,referencedDois,graphApi);
			} catch (BibliographicApiException e) {
				e.printStackTrace();
			}
		}
		return Collections.emptyList();
	}





}
