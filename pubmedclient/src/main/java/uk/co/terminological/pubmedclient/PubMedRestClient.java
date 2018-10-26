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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

/*
 * http://www.ncbi.nlm.nih.gov/books/NBK25500/
 */
public class PubMedRestClient {

	// TODO: integrate CSL: https://michel-kraemer.github.io/citeproc-java/api/1.0.1/de/undercouch/citeproc/csl/CSLItemDataBuilder.html 
	
	
	private static final String DEFAULT_BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private Client client;
	private String apiKey;
	private String appId;
	private String developerEmail;
	private WebResource eSearchResource;
	private WebResource eFetchResource;
	private JAXBContext jcSearch;
	private JAXBContext jcFetch;
	private Unmarshaller searchUnmarshaller;
	private Unmarshaller fetchUnmarshaller;
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(PubMedRestClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";
	private static final String ELINK = "elink.fcgi";
	static Long timestamp = 0L;
	
	public static Map<String, PubMedRestClient> singleton = new HashMap<>();

	public static PubMedRestClient create(String apiKey, String appId, String developerEmail) {
		
		if (!singleton.containsKey(apiKey)) {
			PubMedRestClient tmp = new PubMedRestClient(DEFAULT_BASE_URL, apiKey, appId, developerEmail);
			singleton.put(apiKey, tmp);
		}
		return singleton.get(apiKey);
		
	}
	
	// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
	public PubMedRestClient(String baseUrl, String apiKey, String appId, String developerEmail) {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		try {
			jcSearch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.esearch");
			searchUnmarshaller = jcSearch.createUnmarshaller();
			jcFetch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.efetch");
			fetchUnmarshaller = jcFetch.createUnmarshaller();
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
	
	//Could use google guava - RateLimiter - https://google.github.io/guava/releases/19.0/api/docs/index.html?com/google/common/util/concurrent/RateLimiter.html
	private void rateLimit() {
		while (System.currentTimeMillis() < timestamp+100)
			try {
				synchronized(timestamp) {
					timestamp.wait(5);
				}
			} catch (InterruptedException e1) {
				//probably be OK to continue 
			}
		timestamp = System.currentTimeMillis();
	}

	public ESearchQueryBuilder createESearchQuery() {
		return new ESearchQueryBuilder(defaultApiParams());
	}
	
	public static class ESearchQueryBuilder {
		MultivaluedMap<String, String> searchParams;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		
		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}
		
		protected ESearchQueryBuilder(MultivaluedMap<String, String> searchParams) {
			this.searchParams = searchParams;
			searchParams.remove("db");
			searchParams.add("db", "pubmed");
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
		
		public ESearchQueryBuilder between(Date start, Date end) {
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
		rateLimit();
		InputStream is = builder.get(eSearchResource).get(InputStream.class);
		ESearchResult searchResult;
		try {
			searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
			is.close();
			
		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		return new PubMedResult.Search(searchResult);
	}
	
	

	/**
	 * Fetch PubMed article metadata and abstract
	 * 
	 * @param pmid
	 * @return
	 * @throws BibliographicApiException 
	 * @throws JAXBException
	 */
	public PubMedResult.EntrySet fetchPubmedEntries(List<String> pmids) throws BibliographicApiException {
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		fetchParams.add("id", pmids.stream().collect(Collectors.joining(",")));
		fetchParams.add("format", "xml");
		rateLimit();
		InputStream is = eFetchResource.queryParams(fetchParams).post(InputStream.class);
		Object obj;
		try {
			obj = fetchUnmarshaller.unmarshal(is);
			is.close();
		} catch (JAXBException | IOException e1) {
			throw new BibliographicApiException("Could not parse response:",e1);
		}
		PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) obj;
		return new PubMedResult.EntrySet(pubmedArticleSet);
	}
	
	public Optional<PubMedResult.Entry> fetchPubmedEntry(String pmid) throws BibliographicApiException {
		return fetchPubmedEntries(Collections.singletonList(pmid)).stream().findFirst();
	}

	
	/**
	 * retrieves a full text for an article from PubMed Central
	 * @param pmcId
	 * @return
	 * @throws JAXBException
	 */
	public InputStream fetchPMCFullText(String pmcId) {
		return fetch(Database.PMC, Collections.singletonList(pmcId));
	}

	/**
	 * retrieves a full entries for a list of articles from the given database 
	 * @param list of ids
	 * @return
	 */
	public InputStream fetch(Database db, List<String> ids) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", db.name().toLowerCase());
		params.add("retmode", "xml");
		params.add("id", ids.stream().collect(Collectors.joining(",")));
		logger.debug("making efetch query with params {}", params.toString());
		rateLimit();
		return eFetchResource.queryParams(params).post(InputStream.class);
	}

}