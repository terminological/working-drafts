package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.ELinkResult;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import uk.co.terminological.pubmedclient.PubMedResult.Links;

/*
 * http://www.ncbi.nlm.nih.gov/books/NBK25500/
 */
public class EntrezClient {

	// TODO: integrate CSL: https://michel-kraemer.github.io/citeproc-java/api/1.0.1/de/undercouch/citeproc/csl/CSLItemDataBuilder.html 


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
	private Unmarshaller linkUnmarshaller;
	private JAXBContext jcFetch;
	private Unmarshaller searchUnmarshaller;
	private Unmarshaller fetchUnmarshaller;
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(EntrezClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";
	private static final String ELINK = "elink.fcgi";
	private RateLimiter rateLimiter = RateLimiter.create(10);

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
			searchUnmarshaller = jcSearch.createUnmarshaller();
			jcFetch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.efetch");
			fetchUnmarshaller = jcFetch.createUnmarshaller();
			jcLink = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.elink");
			linkUnmarshaller = jcLink.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException("Problem initialising JAXB",e);
		}
		this.apiKey = apiKey;
		this.developerEmail = developerEmail;
	}

	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("api_key", apiKey);
		out.add("tool", appId);
		out.add("email", developerEmail);
		return out;
	}

	public ESearchQueryBuilder buildSearchQuery() {
		return new ESearchQueryBuilder(defaultApiParams(), this);
	}

	public static class ESearchQueryBuilder {
		MultivaluedMap<String, String> searchParams;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		EntrezClient client;

		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}

		protected ESearchQueryBuilder(MultivaluedMap<String, String> searchParams, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.remove("db");
			this.searchParams.add("db", "pubmed");
			this.client = client;
		}

		public ESearchQueryBuilder searchTerm(String term) {
			searchParams.remove("term");
			this.searchParams.add("term", term);
			return this;
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

		public ESearchQueryBuilder betweenDates(Date start, Date end) {
			searchParams.remove("mindate");
			searchParams.remove("maxdate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("mindate", format.format(start));
			this.searchParams.add("maxdate", format.format(end));
			return this;
		}

		public ESearchQueryBuilder restrictSearchToField(String field) {
			searchParams.remove("field");
			this.searchParams.add("field", field);
			return this;
		}

		public PubMedResult.Search execute() throws BibliographicApiException {
			return client.search(this);
		}
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
	public PubMedResult.Search search(ESearchQueryBuilder builder) throws BibliographicApiException {
		//logger.debug("making esearch query with params {}", queryParams.toString());
		rateLimiter.acquire();
		InputStream is = builder.get(eSearchResource).post(InputStream.class);
		ESearchResult searchResult;
		try {
			searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
			is.close();

		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		return new PubMedResult.Search(searchResult);
	}


	public List<String> findPMIdsBySearch(String searchTerm) throws BibliographicApiException {
		return this.buildSearchQuery().searchTerm(searchTerm).execute().getIds();
	}


	/**
	 * Fetch PubMed article metadata and abstract
	 * 
	 * @param pmid
	 * @return
	 * @throws BibliographicApiException 
	 * @throws JAXBException
	 */
	public PubMedResult.Entries getPMEntriesByPMIds(List<String> pmids) throws BibliographicApiException {
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		fetchParams.add("id", pmids.stream().collect(Collectors.joining(",")));
		fetchParams.add("format", "xml");
		rateLimiter.acquire();
		InputStream is = eFetchResource.queryParams(fetchParams).post(InputStream.class);
		Object obj;
		try {
			obj = fetchUnmarshaller.unmarshal(is);
			is.close();
		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("Could not parse response:",e1);
		}
		PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) obj;
		return new PubMedResult.Entries(pubmedArticleSet);
	}

	public Optional<PubMedResult.Entry> getPMEntryByPMId(String pmid) throws BibliographicApiException {
		return getPMEntriesByPMIds(Collections.singletonList(pmid)).stream().findFirst();
	}

	/**
	 * retrieves a full text for an article from PubMed Central
	 * @param pmcId
	 * @return
	 * @throws JAXBException
	 */
	public InputStream getPubMedCentralFullTextByPubMedCentralId(String pmcId) {
		return getFullTextByIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC);
	}


	public InputStream getPubMedCentralFullTextByPMEntries(PubMedResult.Entries pmEntries) {
		List<String> pmcIds = pmEntries.stream().flatMap(e -> e.getPMCID().stream()).collect(Collectors.toList());
		return getFullTextByIdsAndDatabase(pmcIds, Database.PMC);
	}

	public InputStream getPubMedCentralFullTextByPMEntry(PubMedResult.Entry pmEntry) throws BibliographicApiException {
		String pmcId = pmEntry.getPMCID().orElseThrow(() -> new BibliographicApiException("No PMC id for Entry"));
		return getPubMedCentralFullTextByPubMedCentralId(pmcId);
	}

	/**
	 * retrieves a full entries for a list of articles from the given database 
	 * @param list of ids
	 * @return
	 */
	public InputStream getFullTextByIdsAndDatabase(List<String> ids,Database db) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", db.name().toLowerCase());
		params.add("retmode", "xml");
		params.add("id", ids.stream().collect(Collectors.joining(",")));
		logger.debug("making efetch query with params {}", params.toString());
		rateLimiter.acquire();
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
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
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

		public ELinksQueryBuilder betweenDates(Date start, Date end) {
			searchParams.remove("mindate");
			searchParams.remove("maxdate");
			searchParams.remove("datetype");
			this.searchParams.add("datetype", "edat");
			this.searchParams.add("mindate", format.format(start));
			this.searchParams.add("maxdate", format.format(end));
			return this;
		}

		public Links execute() throws BibliographicApiException {
			return client.link(this);
		}
	}

	public static enum Command {
		NEIGHBOR, NEIGHBOR_SCORE, PRLINKS, LLINKS
	}


	public PubMedResult.Links link(ELinksQueryBuilder builder) throws BibliographicApiException {
		rateLimiter.acquire();
		InputStream is = builder.get(eLinkResource).post(InputStream.class);
		ELinkResult linkResult;
		try {
			linkResult = (ELinkResult) linkUnmarshaller.unmarshal(is);
			is.close();

		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		return new PubMedResult.Links(linkResult);
	}

	public List<String> getSimilarPMIdsByPMId(List<String> pmids) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.command(Command.NEIGHBOR_SCORE)
				.withLinkname("pubmed_pubmed")
				.execute().stream()
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}

	public List<String> getPubMedCentralIdsByPMId(List<String> pmids) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pmc")
				.execute().stream()
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}

	public List<String> getReferencedPMIdsByPMId(List<String> pmids) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PUBMED)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pubmed_refs")
				.execute().stream()
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
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