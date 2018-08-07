package uk.co.terminological.pubmedclient;

import gov.nih.nlm.ncbi.eutils.generated.articleset.PmcArticleset;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;
import gov.nih.nlm.ncbi.eutils.generated.esearch.Count;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import gov.nih.nlm.ncbi.eutils.generated.esummary.DocSum;
import gov.nih.nlm.ncbi.eutils.generated.esummary.ESummaryResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/*
 * http://www.ncbi.nlm.nih.gov/books/NBK25500/
 */
public class PubMedRestClient {

	private static final String DEFAULT_BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	private Client client;
	private String apiKey;
	private String appId;
	private String developerEmail;
	private WebResource eSearchResource;
	private WebResource eFetchResource;
	private JAXBContext jcSearch;
	private JAXBContext jcFetch;
	private JAXBContext pmcJaxbContext;
	private Unmarshaller searchUnmarshaller;
	private Unmarshaller fetchUnmarshaller;
	private Unmarshaller pmcUnmarshaller;
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(PubMedRestClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";
	static Long timestamp = 0L;
	

	protected PubMedRestClient(String apiKey, String appId, String developerEmail) { 
		this(DEFAULT_BASE_URL, apiKey, appId, developerEmail);
	}

	// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
	protected PubMedRestClient(String baseUrl, String apiKey, String appId, String developerEmail) {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		try {
			jcSearch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.esearch");
			searchUnmarshaller = jcSearch.createUnmarshaller();
			jcFetch = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.efetch");
			fetchUnmarshaller = jcFetch.createUnmarshaller();
			pmcJaxbContext = JAXBContext.newInstance("gov.nih.nlm.ncbi.eutils.generated.articleset");
			pmcUnmarshaller = pmcJaxbContext.createUnmarshaller();
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

	/**
	 * Retrieve PMIDs from PubMed for a search string
	 * 
	 * @param searchTerm
	 *            search string (same as Pubmed web interface)
	 * @return
	 * @throws JAXBException
	 */
	protected ESearchResult searchPubmed(String searchTerm, int returnMax) throws JAXBException {
		MultivaluedMap<String, String> searchParams = defaultApiParams();
		searchParams.add("db", "pubmed");
		searchParams.add("term", searchTerm);
		searchParams.add("retMax", ""+returnMax);
		return search(searchParams);
	}

	/**
	 * Retrieve PMIDs from PubMed for a article title
	 * 
	 * @param the
	 *            title of the article
	 * @return
	 * @throws JAXBException
	 */
	protected ESearchResult searchPubmedByTitle(String title) throws JAXBException {
		MultivaluedMap<String, String> searchParams = defaultApiParams();
		searchParams.add("db", "pubmed");
		searchParams.add("field", "title");
		searchParams.add("term", title);
		return search(searchParams);
	}

	/**
	 * Fetch PubMed article metadata and abstract
	 * 
	 * @param pmid
	 * @return
	 * @throws JAXBException
	 */
	protected PubmedArticle fetchPubmedArticle(long pmid) throws JAXBException {
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		fetchParams.add("id", String.valueOf(pmid));
		fetchParams.add("format", "xml");
		PubmedArticleSet pubmedArticleSet = fetch(fetchParams);
		if (pubmedArticleSet != null) {
			List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
			if (objects.size() == 1) {
				if (objects.get(0) instanceof PubmedArticle) {
					PubmedArticle pubmedArticle = (PubmedArticle) objects.get(0);
					return pubmedArticle;
				}
			}
		}
		throw new IllegalStateException();
	}

	/*
	 * Pubmed central:
	 * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?
	 * db=pmc&field=title
	 * &term=Accuracy%20of%20single%20progesterone%20test%20to%20predict%
	 * 20early%20pregnancy%20outcome%20in%20women%20with%20pain%20or%20bleeding:%20meta-analysis%20of%20cohort%20studies.
	 */
	protected ESearchResult search(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making esearch query with params {}", queryParams.toString());
		rateLimit();
		InputStream is = eSearchResource.queryParams(queryParams).get(InputStream.class);
		ESearchResult searchResult = (ESearchResult) searchUnmarshaller.unmarshal(is);
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		List<Object> objects = searchResult
				.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR();
		for (Object object : objects) {
			if (object instanceof Count) {
				Count count = (Count) object;
				logger.debug("results count {}", count.getvalue());
				break;
			}
		}
		return searchResult;
	}

	// http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=11850928,11482001&format=xml
		/*
		 * Pubmed central:
		 * https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=protein&id=6678417,9507199,28558982,28558984,28558988,28558990
		 */
	protected ESummaryResult summary(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making efetch query with params {}", queryParams.toString());
		rateLimit();
		InputStream is = eFetchResource.queryParams(queryParams).post(InputStream.class);
		Object obj = fetchUnmarshaller.unmarshal(is);
		ESummaryResult out = (ESummaryResult) obj;
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", out.getDocSumOrERROR().stream().filter(a -> a instanceof DocSum).map(a -> (DocSum) a).count());
		return out;
	}
	
	// http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=11850928,11482001&format=xml
	/*
	 * Pubmed central:
	 * http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db
	 * =pmc&id=3460254
	 */
	protected PubmedArticleSet fetch(MultivaluedMap<String, String> queryParams) throws JAXBException {
		logger.debug("making efetch query with params {}", queryParams.toString());
		rateLimit();
		InputStream is = eFetchResource.queryParams(queryParams).post(InputStream.class);
		Object obj = fetchUnmarshaller.unmarshal(is);
		PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) obj;
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", pubmedArticleSet.getPubmedArticleOrPubmedBookArticle().size());
		return pubmedArticleSet;
	}

	/**
	 * Utility method to retrieve the Mesh Headings for an PubMed article
	 * 
	 * @param pmid
	 * @return
	 * @throws JAXBException
	 */
	protected MeshHeadingList fetchMeshHeadingsForPubmedArticle(long pmid) throws JAXBException {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", "pubmed");
		params.add("retmode", "xml");
		params.add("id", String.valueOf(pmid));
		PubmedArticleSet pubmedArticleSet = fetch(params);
		List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
		if (objects.size() == 1) {
			PubmedArticle pubmedArticle = (PubmedArticle) objects.get(0);
			return pubmedArticle.getMedlineCitation().getMeshHeadingList();
		}
		throw new IllegalStateException();
	}

	/**
	 * Search PubMed Central for full text articles using a query string.
	 * 
	 * @param searchTerm
	 *            the searchterm same as the pubmed central web interface
	 * @return
	 * @throws JAXBException
	 */
	protected ESearchResult seachPubmedCentral(String searchTerm) throws JAXBException {
		MultivaluedMap<String, String> searchParams = new MultivaluedMapImpl();
		searchParams.add("db", "pmc");
		searchParams.add("term", searchTerm);
		return search(searchParams);
	}

	/**
	 * Search PubMed Central for full text articles using a query string.
	 * 
	 * @param title
	 * @return
	 * @throws JAXBException
	 */
	protected ESearchResult seachPubmedCentralByTitle(String title) throws JAXBException {
		MultivaluedMap<String, String> searchParams = defaultApiParams();
		searchParams.add("db", "pmc");
		searchParams.add("field", "title");
		searchParams.add("term", title);
		return search(searchParams);
	}

	/**
	 * retrieves a full text for an article from PubMed Central
	 * @param pmcId
	 * @return
	 * @throws JAXBException
	 */
	protected PmcArticleset fetchFullTextArticle(String pmcId) throws JAXBException {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", "pmc");
		params.add("retmode", "xml");
		params.add("id", String.valueOf(pmcId));
		return pmcFetch(params);
	}

	protected PmcArticleset pmcFetch(MultivaluedMap<String, String> params) throws JAXBException {
		logger.debug("making efetch query with params {}", params.toString());
		rateLimit();
		InputStream is = eFetchResource.queryParams(params).post(InputStream.class);
		Object obj = pmcUnmarshaller.unmarshal(is);
		PmcArticleset pmcArticleset = (PmcArticleset) obj;
		try {
			is.close();
		} catch (IOException e) {
			logger.error("could not close ioStream", e);
		}
		logger.debug("results count {}", pmcArticleset.getArticle().size());
		return pmcArticleset;
	}

	

}