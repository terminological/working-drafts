package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.util.concurrent.RateLimiter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.pubmedclient.CrossRefResult.ListResult;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;

public class CrossRefClient {
	// https://www.crossref.org/schemas/
	// https://github.com/CrossRef/rest-api-doc/blob/master/api_format.md
	// https://github.com/CrossRef/rest-api-doc
	// http://tdmsupport.crossref.org/researchers/
	// http://clickthroughsupport.crossref.org/
	
	// TODO: integrate CSL: https://michel-kraemer.github.io/citeproc-java/api/1.0.1/de/undercouch/citeproc/csl/CSLItemDataBuilder.html
	
	private static Map<String,CrossRefClient> singleton = new HashMap<>();
	
	private CrossRefClient(String developerEmail) {
		this.developerEmail = developerEmail;
		this.client = Client.create();
	}

	private static final Logger logger = LoggerFactory.getLogger(CrossRefClient.class);
	
	private String developerEmail;
	private Client client;
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	private RateLimiter rateLimiter = RateLimiter.create(50);
	
	
	public static CrossRefClient create(String developerEmail) {
		if (singleton.containsKey(developerEmail)) return singleton.get(developerEmail);
		CrossRefClient tmp = new CrossRefClient(developerEmail);
		singleton.put(developerEmail, tmp);
		return tmp;
	};
	
	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("mailto", developerEmail);
		return out;
	}
	
	public static Predicate<String> ACCEPT_ANY_LICENCE = new Predicate<String>() {
		@Override
		public boolean test(String t) {
			return true;
	}};
	
	public static Predicate<String> ACCEPT_CREATIVE_COMMONS = new Predicate<String>() {
		@Override
		public boolean test(String t) {
			return t.startsWith("http://creativecommons.org");
	}};
	
	
	
	
	private static String encode(String string)  {
		try {
			return URLEncoder.encode(string,java.nio.charset.StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	static String baseUrl ="http://api.crossref.org/";
	
	
	
	private void updateRateLimits(MultivaluedMap<String, String> headers) {
		try {
			
			Float rateLimitRequests = Float.parseFloat(headers.get("X-Rate-Limit-Limit").get(0));
			Float rateLimitInterval = Float.parseFloat(headers.get("X-Rate-Limit-Interval").get(0).replace("s", ""));
			rateLimiter.setRate(Math.floor(rateLimitRequests/rateLimitInterval));
		} catch (Exception e) {
			//Probably header wasn't set - just ignore
		}
	}

	
	public SingleResult getByDoi(String doi) throws BibliographicApiException {
		rateLimiter.acquire(1);
		logger.debug("Retrieving crossref record for:" + doi);
		String url = baseUrl+"works/"+encode(doi);
		WebResource wr = client.resource(url).queryParams(defaultApiParams());
		try {
			ClientResponse r = wr.get(ClientResponse.class);
			updateRateLimits(r.getHeaders());
			InputStream is = r.getEntityInputStream(); 
			CrossRefResult.SingleResult  response = objectMapper.readValue(is, CrossRefResult.SingleResult.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			throw new BibliographicApiException("Malformed response to: "+url);
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect to: "+url);
		}
	}
	
	public ListResult getByQuery(QueryBuilder qb) throws BibliographicApiException, NoSuchElementException {
		rateLimiter.acquire(1);
		logger.debug("Querying crossref: "+qb.toString());
		try {
			ClientResponse r = qb.get(client).post(ClientResponse.class);
			updateRateLimits(r.getHeaders());
			InputStream is = r.getEntityInputStream(); 
			CrossRefResult.ListResult  response = objectMapper.readValue(is, CrossRefResult.ListResult.class);
			//Check to see if the result is past the end of the set.
			if (
					response.message.isPresent() && 
					response.message.get().items.size() == 0 && 
					response.message.get().totalResults.orElse(0) > 0) {
				throw new NoSuchElementException();
			}
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			throw new BibliographicApiException("Malformed response to: "+qb.get(client).getURI());
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect to: "+qb.get(client).getURI());
		}
	}
	
	public InputStream getTDM(CrossRefResult.Work work, Predicate<String> licenceFilter, String clickThroughToken) throws BibliographicApiException {
		
		logger.debug("Retrieving crossref content for:" + work.DOI+": "+work.title.get(0));
		
		if (work.license.stream().map(l -> l.URL.toString()).anyMatch(licenceFilter)) {
			
			Optional<URL> url = work.link.stream()
					.filter(rl -> rl.intendedApplication.orElse("").equals("text-mining"))
					.flatMap(rl -> rl.URL.stream())
					.findFirst();
			if (!url.isPresent()) throw new BibliographicApiException("no content for intended application of text-mining");
			
			//TODO: try and find correct type for output, or specify xml. 
			
			WebResource tdmCopy = client.resource(url.get().toString());
			tdmCopy.header("CR-Clickthrough-Client-Token", clickThroughToken);
			ClientResponse r = tdmCopy.get(ClientResponse.class);
			return r.getEntityInputStream();
			
		} else {
			throw new BibliographicApiException("no licensed content found");
		}
		
	}
		
	public QueryBuilder buildQuery() {
		QueryBuilder out = new QueryBuilder(baseUrl+"works", defaultApiParams() , this);
		return out;
	}
	
	public static class QueryBuilder {
		protected String url;
		
		MultivaluedMap<String, String> params;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		CrossRefClient client;
		
		private WebResource get(Client client) {
			WebResource tdmCopy = client.resource(url);
			tdmCopy.queryParams(params);
			return tdmCopy;
		}
		
		private QueryBuilder(String url, MultivaluedMap<String, String> defaultParams, CrossRefClient client ) {
			this.url=url;
			this.params=defaultParams;
			this.client = client;
		}
		
		public QueryBuilder withSearchTerm(String search) {
			params.add("query", search);
			return this;
		}
		
		public QueryBuilder withSearchTerm(Field field, String search) {
			params.add("query."+field.name().toLowerCase().replace("__", ".").replace("_", "-"), search);
			return this;
		}
		
		public QueryBuilder sortedBy(Sort sort, SortOrder order) {
			params.add("sort",sort.name().toLowerCase().replace("__", ".").replace("_", "-"));
			params.add("order",order.name().toLowerCase().replace("__", ".").replace("_", "-"));
			return this;
		}
		
		public QueryBuilder limit(Integer offset, Integer rows) {
			params.add("rows",rows.toString());
			params.add("offset",offset.toString());
			return this;
		}
		
		public QueryBuilder since(Date date) {
			params.add("filter","from-index-date:"+format.format(date));
			return this;
		}
		
		public QueryBuilder firstPage(Integer rows) {
			params.add("rows",rows.toString());
			params.add("cursor", "*");
			return this;
		}
		
		public Optional<QueryBuilder> nextPage(ListResult resp) {
			if (
					resp.message.isPresent() && 
					resp.message.get().nextCursor.isPresent() &&
					!(
							resp.message.get().items.size() == 0 && 
							resp.message.get().totalResults.orElse(0) > 0
					)
				) {
				params.remove("cursor");
				params.add("cursor", resp.message.get().nextCursor.get());
				return Optional.of(this);
			} else {
				return Optional.empty();
			}
			
		}
		
		public QueryBuilder filteredBy(BooleanFilter filter, Boolean value) {
			params.add("filter",filter.name().toLowerCase().replace("__", ".").replace("_", "-")+":"+value.toString());
			return this;
		}
		
		public QueryBuilder filteredBy(StringFilter filter, String value) {
			params.add("filter",filter.name().toLowerCase().replace("__", ".").replace("_", "-")+":"+value.toString());
			return this;
		}
		
		public QueryBuilder filteredBy(DateFilter filter, Date value) {
			params.add("filter",filter.name().toLowerCase().replace("__", ".").replace("_", "-")+":"+format.format(value));
			return this;
		}
		
		public ListResult execute() throws BibliographicApiException {
			return client.getByQuery(this);
		}
		
		public String toString() {
			return url+": "+params.toString(); 
		}
	}
	
	public static enum Field {
		TITLE,
		CONTAINER_TITLE,
		AUTHOR,
		EDITOR,
		CHAIR,
		TRANSLATOR,
		CONTRIBUTOR,
		BIBLIOGRAPHIC,
		AFFIILIATION
	}
	
	public static enum Sort {
		SCORE,
		RELEVANCE,
		UPDATED,
		DEPOSITED,
		INDEXED,
		PUBLISHED,
		PUBLISHED_PRINT,
		PUBLISHED_ONLINE,
		ISSUED,
		IS_REFERENCED_BY_COUNT,
		REFERENCES_COUNT
	}
	
	public static enum SortOrder { ASC,DESC }
	
	public static enum BooleanFilter {
		HAS_FUNDER,
		HAS_LICENSE,
		HAS_FULL_TEXT,
		HAS_REFERENCES,
		HAS_ARCHIVE,
		HAS_ORCHID,
		HAS_AUTHENTICATED_ORCHID,
		IS_UPDATE,
		HAS_UPDATE_POLICY,
		HAS_ASSERTION,
		HAS_AFFILIATION,
		HAS_ABSTRACT,
		HAS_CLINICAL_TRIAL_NUMBER,
		HAS_CONTENT_DOMAIN,
		HAS_DOMAIN_RESTRICTION,
		HAS_RELATION
		
	}
	
	public static enum DateFilter {
		FROM_INDEX_DATE,
		UNTIL_INDEX_DATE,
		FROM_DEPOSIT_DATE,
		UNTIL_DEPOSIT_DATE,
		FROM_UPDATE_DATE,
		UNTIL_UPDATE_DATE,
		FROM_CREATED_DATE,
		UNTIL_CREATED_DATE,
		FROM_PUB_DATE,
		UNTIL_PUB_DATE,
		FROM_ONLINE_PUB_DATE,
		UNTIL_ONLINE_PUB_DATE,
		FROM_PRINT_PUB_DATE,
		UNTIL_PRINT_PUB_DATE,
		FROM_POSTED_DATE,
		UNTIL_POSTED_DATE,
		FROM_ACCEPTED_DATE,
		UNTIL_ACCEPTED_DATE
	}
	
	public static enum StringFilter {
		FUNDER,
		LOCATION,
		PREFIX,
		MEMBER,
		LICENSE__URL,
		LICENSE__VERSION,
		LICENSE__DELAY,
		FULL_TEXT__VERSION,
		FULL_TEXT__TYPE,
		FULL_TEXT__APPLICATION,
		REFERENCE_VISIBILITY,
		ORCHID,
		ISSN,
		ISBN,
		TYPE,
		DIRECTORY,
		DOI,
		UPDATES,
		CONTAINER_TITLE,
		CATEGORY_NAME,
		TYPE_NAME,
		AWARD__NUMBER,
		AWARD__FUNDER,
		ASSERTION_GROUP,
		ASSERTION,
		ALTERNATIVE_ID,
		ARTICLE_NUMBER,
		CONTENT_DOMAIN,
		RELATION__TYPE,
		RELATION__OBJECT,
		RELATION__OBJECT_TYPE
		
	}
	
	
	
}
