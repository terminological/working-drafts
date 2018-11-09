package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import gov.nih.nlm.ncbi.eutils.generated.elink.ELinkResult;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.pubmedclient.EntrezResult.Links;

/*
 * http://www.ncbi.nlm.nih.gov/books/NBK25500/
 */
public class EntrezClient {

	// TODO: integrate CSL: https://michel-kraemer.github.io/citeproc-java/api/1.0.1/de/undercouch/citeproc/csl/CSLItemDataBuilder.html 
	// TODO: caching with https://hc.apache.org/httpcomponents-client-ga/tutorial/html/caching.html#storage and EHCache

	private static final String DEFAULT_BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private Client client;
	private String apiKey;
	private String appId;
	private String developerEmail;
	private WebResource eSearchResource;
	private WebResource eFetchResource;
	private WebResource eLinkResource;
	private JAXBContext jcSearch;
	private JAXBContext jcLink;
	//private Unmarshaller linkUnmarshaller;
	//private Unmarshaller searchUnmarshaller;
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(EntrezClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";
	private static final String ELINK = "elink.fcgi";
	private TokenBucket rateLimiter = TokenBuckets.builder().withInitialTokens(10).withCapacity(10).withFixedIntervalRefillStrategy(10, 1, TimeUnit.SECONDS).build();

	public static Map<String, EntrezClient> singleton = new HashMap<>();

	public static EntrezClient create(String apiKey, String appId, String developerEmail) {

		if (!singleton.containsKey(apiKey)) {
			EntrezClient tmp = new EntrezClient(DEFAULT_BASE_URL, apiKey, appId, developerEmail);
			singleton.put(apiKey, tmp);
		}
		return singleton.get(apiKey);

	}

	// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
	public EntrezClient(String baseUrl, String apiKey, String appId, String developerEmail) {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		eLinkResource = client.resource(this.baseUrl + ELINK);
		try {
			jcSearch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.esearch");
			
			jcLink = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.elink");
			
		} catch (JAXBException e) {
			throw new RuntimeException("Problem initialising JAXB",e);
		}
		this.apiKey = apiKey;
		this.appId = appId;
		this.developerEmail = developerEmail;
	}

	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("api_key", apiKey);
		out.add("tool", appId);
		out.add("email", developerEmail);
		return out;
	}

	public ESearchQueryBuilder buildSearchQuery(String searchTerm) {
		return new ESearchQueryBuilder(defaultApiParams(), searchTerm, this);
	}

	public static class ESearchQueryBuilder {
		MultivaluedMap<String, String> searchParams;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		EntrezClient client;

		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}

		protected ESearchQueryBuilder(MultivaluedMap<String, String> searchParams, String searchTerm, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.remove("db");
			this.searchParams.add("term", searchTerm);
			this.searchParams.add("db", "pubmed");
			this.client = client;
		}

		public ESearchQueryBuilder searchDatabase(Database db) {
			searchParams.remove("db");
			this.searchParams.add("db", db.name().toLowerCase());
			return this;
		}

		public ESearchQueryBuilder limit(int from, int count) {
			searchParams.remove("retstart");
			searchParams.remove("retmax");
			this.searchParams.add("retstart", Integer.toString(from));
			this.searchParams.add("retmax", Integer.toString(count));
			return this;
		}

		public ESearchQueryBuilder withinLastDays(int count) {
			searchParams.remove("reldate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("reldate", Integer.toString(count));
			return this;
		}

		public ESearchQueryBuilder betweenDates(LocalDate start, LocalDate end) {
			searchParams.remove("mindate");
			searchParams.remove("maxdate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("mindate", start.format(formatter));
			this.searchParams.add("maxdate", end.format(formatter));
			return this;
		}

		public ESearchQueryBuilder restrictSearchToField(String field) {
			searchParams.remove("field");
			this.searchParams.add("field", field);
			return this;
		}

		public EntrezResult.Search execute() throws BibliographicApiException {
			return client.search(this);
		}
		
		public String toString() {return searchParams.toString();}
	}

	//public static class ELinkQueryBuilder

	public static enum Database { PUBMED, PMC, MESH, GENE }

	/**
	 * Retrieve PMIDs from PubMed for a search string
	 * 
	 * @param searchTerm
	 *            search string (same as Pubmed web interface)
	 * @return
	 * @throws BibliographicApiException 
	 * @throws JAXBException
	 */
	public EntrezResult.Search search(ESearchQueryBuilder builder) throws BibliographicApiException {
		logger.debug("making esearch query with params {}", builder.toString());
		rateLimiter.consume();
		InputStream is = builder.get(eSearchResource).post(InputStream.class);
		ESearchResult searchResult;
		
		try {
			Unmarshaller searchUnmarshaller = jcSearch.createUnmarshaller();
			searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
			is.close();

		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		return new EntrezResult.Search(searchResult);
	}


	public List<String> findPMIdsBySearch(String searchTerm) throws BibliographicApiException {
		return this.buildSearchQuery(searchTerm).execute().getIds();
	}

	public List<String> findPMIdsByDois(List<String> dois) throws BibliographicApiException {
		return this.buildSearchQuery(dois.stream().collect(Collectors.joining(" OR "))).execute().getIds();
	}

	/**
	 * Fetch PubMed article metadata and abstract
	 * 
	 * @param pmid
	 * @return
	 * @throws BibliographicApiException 
	 * @throws JAXBException
	 */
	public EntrezResult.PubMedEntries getPMEntriesByPMIds(List<String> pmids) throws BibliographicApiException {
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		pmids.forEach(id -> fetchParams.add("id",id));
		fetchParams.add("format", "xml");
		rateLimiter.consume();
		logger.debug("making efetch query with params {}", fetchParams.toString());
		InputStream is = eFetchResource.queryParams(fetchParams).post(InputStream.class);
		Xml xml;
		try {
			xml = Xml.fromStream(is);
		} catch (XmlException e) {
			throw new BibliographicApiException("could not parse result",e);
		}
		return new EntrezResult.PubMedEntries(xml.content());
	}

	public Optional<EntrezResult.PubMedEntry> getPMEntryByPMId(String pmid) throws BibliographicApiException {
		return getPMEntriesByPMIds(Collections.singletonList(pmid)).stream().findFirst();
	}

	/**
	 * retrieves a full text for an article from PubMed Central
	 * @param pmcId
	 * @return
	 * @throws JAXBException
	 */
	public InputStream getPubMedCentralXMLByPubMedCentralId(String pmcId) {
		return getXMLByIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC);
	}


	public InputStream getPubMedCentralXMLByPMEntries(EntrezResult.PubMedEntries pmEntries) {
		List<String> pmcIds = pmEntries.stream().flatMap(e -> e.getPMCID().stream()).collect(Collectors.toList());
		return getXMLByIdsAndDatabase(pmcIds, Database.PMC);
	}

	public InputStream getPubMedCentralXMLByPMEntry(EntrezResult.PubMedEntry pmEntry) throws BibliographicApiException {
		String pmcId = pmEntry.getPMCID().orElseThrow(() -> new BibliographicApiException("No PMC id for Entry"));
		return getPubMedCentralXMLByPubMedCentralId(pmcId);
	}

	/**
	 * retrieves a full entries for a list of articles from the given database 
	 * @param list of ids
	 * @return
	 */
	public InputStream getXMLByIdsAndDatabase(List<String> ids,Database db) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", db.name().toLowerCase());
		params.add("retmode", "xml");
		params.add("id", ids.stream().collect(Collectors.joining(",")));
		logger.debug("making efetch query with params {}", params.toString());
		rateLimiter.consume();
		return eFetchResource.queryParams(params).post(InputStream.class);
	}


	/**
	 * Elinks
	 * @return
	 */
	public ELinksQueryBuilder buildLinksQueryForIdsAndDatabase(List<String> ids, Database fromDb) {
		return new ELinksQueryBuilder(defaultApiParams(),ids, fromDb, this);
	}

	public static class ELinksQueryBuilder {
		MultivaluedMap<String, String> searchParams;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		EntrezClient client;

		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}

		protected ELinksQueryBuilder(MultivaluedMap<String, String> searchParams, List<String> ids, Database fromDb, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.add("dbfrom", fromDb.name().toLowerCase());
			this.searchParams.add("cmd", "neighbour_score");
			this.searchParams.add("retmode", "xml");
			ids.forEach(id -> this.searchParams.add("id", id));
			this.client = client;
		}

		public ELinksQueryBuilder command(Command command) {
			searchParams.remove("cmd");
			this.searchParams.add("cmd", command.name().toLowerCase());
			return this;
		}

		public ELinksQueryBuilder toDatabase(Database to) {
			searchParams.remove("db");
			this.searchParams.add("db", to.name().toLowerCase());
			return this;
		}

		public ELinksQueryBuilder withLinkname(String linkName) {
			this.searchParams.add("linkname", linkName);
			return this;
		}

		public ELinksQueryBuilder searchLinked(String term) {
			searchParams.remove("term");
			this.searchParams.add("term", term);
			return this;
		}

		public ELinksQueryBuilder withinLastDays(int count) {
			searchParams.remove("reldate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("reldate", Integer.toString(count));
			return this;
		}

		public ELinksQueryBuilder betweenDates(LocalDate start, LocalDate end) {
			searchParams.remove("mindate");
			searchParams.remove("maxdate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("mindate", start.format(formatter));
			this.searchParams.add("maxdate", end.format(formatter));
			return this;
		}

		public Links execute() throws BibliographicApiException {
			return client.link(this);
		}
		
		public String toString() {return searchParams.toString();}
	}

	public static enum Command {
		NEIGHBOR, NEIGHBOR_SCORE, PRLINKS, LLINKS
	}


	public EntrezResult.Links link(ELinksQueryBuilder builder) throws BibliographicApiException {
		logger.debug("making elink query with params {}", builder.toString());
		rateLimiter.consume();
		InputStream is = builder.get(eLinkResource).post(InputStream.class);
		ELinkResult linkResult;
		try {
			Unmarshaller linkUnmarshaller = jcLink.createUnmarshaller();
			linkResult = (ELinkResult) linkUnmarshaller.unmarshal(is);
			is.close();

		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		return new EntrezResult.Links(linkResult);
	}

	public Map<String,Long> getSimilarScoredPMIdsByPMId(String pmid) throws BibliographicApiException {
		Map<String,Long> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmid), Database.PUBMED)
			.command(Command.NEIGHBOR_SCORE)
			.withLinkname("pubmed_pubmed")
			.execute().stream()
			.forEach(l -> {
				l.toId.ifPresent(to -> out.put(to, l.score.orElse(0L)));
			});
		return out;
	}

	public Map<String,String> getPubMedCentralIdsByPMId(List<String> pmids) throws BibliographicApiException {
		Map<String,String> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pmc")
				.execute().stream()
				.forEach(l -> l.toId.ifPresent(to -> out.put(l.fromId, to)));
		return out;
	}

	public Map<String,String> getReferencedPMIdsByPMId(List<String> pmids) throws BibliographicApiException {
		Map<String,String> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PUBMED)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pubmed_refs")
				.execute().stream()
				.forEach(l -> l.toId.ifPresent(to -> out.put(l.fromId, to)));
		return out;
	}

	public List<String> getReferencingPubMedCentralIdsByPubMedCentralId(String pmcId) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pmc_pmc_citedby")
				.execute().stream()
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}

	public List<String> getReferencedPubMedCentralIdsByPubMedCentralId(String pmcId) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pmc_pmc_cites")
				.execute().stream()
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}
	
}