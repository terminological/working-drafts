package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.pubmedclient.CrossRefApiResponse.ListResponse;
import uk.co.terminological.pubmedclient.CrossRefApiResponse.Response;

public class CrossRefClient {
	// https://www.crossref.org/schemas/
	// https://github.com/CrossRef/rest-api-doc/blob/master/api_format.md
	// https://github.com/CrossRef/rest-api-doc
	// http://tdmsupport.crossref.org/researchers/
	// http://clickthroughsupport.crossref.org/
	
	private String clickThroughToken;
	private String developerEmail;
	private Client client;
	private Integer rateLimitRequests = 50;
	private Long rateLimitInterval = 1000L;
	private Long lastIntervalStart = System.currentTimeMillis();
	private Integer intervalRequests = 0;
	
	public static class CrossRefException extends Exception {
		public CrossRefException(String string) {
			super(string);
		}
	}
	
	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		//out.add("api_key", apiKey);
		//out.add("tool", appId);
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
	
	public InputStream getTDM(CrossRefApiResponse.Work work, Predicate<String> licenceFilter) throws CrossRefException {
		
		if (work.license.stream().map(l -> l.URL.toString()).anyMatch(licenceFilter)) {
			
			Optional<URL> url = work.link.stream().filter(rl -> rl.intendedApplication.equals("text-mining")).map(rl -> rl.URL).findFirst();
			if (!url.isPresent()) throw new CrossRefException("no content for intended application of text-mining");
			
			MultivaluedMap<String, String> searchParams = defaultApiParams();
			searchParams.add("CR-Clickthrough-Client-Token", clickThroughToken);
			WebResource tdmCopy = client.resource(url.get().toString());
			return tdmCopy.queryParams(searchParams).get(InputStream.class);
			
		} else {
			throw new CrossRefException("no licensed content found");
		}
		
	}
	
	
	private static String encode(String string)  {
		try {
			return URLEncoder.encode(string,java.nio.charset.StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	static String baseUrl ="http://api.crossref.org/";
	
	public Response getByDoi(String doi) {
		String url = baseUrl+"works/"+encode(doi);
		//TODO: execute query 
		return null;
	}
	
	
	
	public ListResponse getByQuery(QueryBuilder qb) {
		//TODO: execute query 
		return null;
	}
		
	public static class QueryBuilder {
		protected String url;
		
		MultivaluedMap<String, String> params = defaultApiParams();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		public static QueryBuilder create() {
			QueryBuilder out = new QueryBuilder(baseUrl+"works");
			return out;
		}
		
		private QueryBuilder(String url) {this.url=url;}
		
		public QueryBuilder withSearchTerm(String search) {
			params.add("query", search);
			return this;
		}
		
		public QueryBuilder withSearchTerm(Field field, String search) {
			params.add("query."+field.name().toLowerCase().replace("_", "-"), search);
			return this;
		}
		
		public QueryBuilder sortedBy(Sort sort, SortOrder order) {
			params.add("sort",sort.name().toLowerCase().replace("_", "-"));
			params.add("order",order.name().toLowerCase().replace("_", "-"));
			return this;
		}
		
		public QueryBuilder filteredBy(BooleanFilter filter, Boolean value) {
			params.add("filter",filter.name().toLowerCase().replace("_", "-")+":"+value.toString());
			return this;
		}
		
		public QueryBuilder filteredBy(StringFilter filter, String value) {
			params.add("filter",filter.name().toLowerCase().replace("_", "-")+":"+value.toString());
			return this;
		}
		
		public QueryBuilder filteredBy(DateFilter filter, Date value) {
			params.add("filter",filter.name().toLowerCase().replace("_", "-")+":"+format.format(value));
			return this;
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
		
	}
	
	
	
}
