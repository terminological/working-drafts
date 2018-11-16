package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapCrossRefReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapEntriesToNode;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralCitedBy;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubMedCentralReferences;
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapPubmedRelated;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.*;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Props.*;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.*;

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
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pubmedclient.BibliographicApiException;
import uk.co.terminological.pubmedclient.BibliographicApis;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;
import uk.co.terminological.pubmedclient.EntrezClient.Command;
import uk.co.terminological.pubmedclient.EntrezClient.Database;
import  uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.EntrezResult.Search;
import uk.co.terminological.pubmedclient.IdConverterClient.IdType;
import uk.co.terminological.pubmedclient.IdConverterClient.Record;

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
		
		if (!graphApi.get().schema().getIndexes().iterator().hasNext()) {
			PubMedGraphSchema.setupSchema(graphApi);
		}

		//TODO: Main loop
		Optional<Search> broadSearch = searchPubMed(this.broaderSearch);
		Optional<Search> narrowSearchIds = searchPubMed(this.search);

		Optional<PubMedEntries> entries = broadSearch.flatMap(s -> {
			try {
				return s.getStoredResult(biblioApi.getEntrez());
			} catch (BibliographicApiException e) {
				return Optional.empty();
			}
		});
		
		entries.ifPresent(ent -> {
			mapEntriesToNode(ent, graphApi, earliest, latest, false);
		});
		
		List<String> pmids = narrowSearchIds.get().getIds().collect(Collectors.toList());
		PubMedGraphUtils.addLabelsByIds(ARTICLE, PMID, pmids, EXPAND, graphApi);
		
		
		
		
		graphApi.waitAndShutdown();

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


	List<Record> lookupIds(List<String> ids, IdType ofType) {
		List<Record> out = new ArrayList<>();

		try {
			while(ids.size() > 0) {
				List<String> batchDois = ids.subList(0, Math.min(100, ids.size()));
				List<Record> pmids = biblioApi.getPmcIdConv()
						.getConverterForIdsAndType(batchDois, ofType)
						.records
						;
				log.info("Looked up "+batchDois.size()+" "+ofType.name()+" and found "+pmids.size()+" linked records");
				out.addAll(pmids);
				ids.subList(0, Math.min(100, ids.size())).clear();
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
