package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;

import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.pubmedclient.CrossRefResult.ListResult;
import uk.co.terminological.pubmedclient.CrossRefResult.SingleResult;
import uk.co.terminological.pubmedclient.CrossRefResult.Work;

public class CrossRefClient extends CachingApiClient {
	// https://www.crossref.org/schemas/
	// https://github.com/CrossRef/rest-api-doc/blob/master/api_format.md
	// https://github.com/CrossRef/rest-api-doc
	// http://tdmsupport.crossref.org/researchers/
	// http://clickthroughsupport.crossref.org/

	// XML format also available:
	// http://api.crossref.org/works/10.1038/ncomms6976.xml
	// http://api.crossref.org/works/10.1038/ncomms6976/transform/application/vnd.crossref.unixsd+xml
	// http://www.crossref.org/schemas/crossref_query_output3.0.xsd

	// https://support.crossref.org/hc/en-us/articles/214318946-Retrieving-cited-by-matches
	// https://doi.crossref.org/servlet/getForwardLinks?usr=xxx&pwd=xxx&doi=10.5555/12345678&include_postedcontent=true
	// https://doi.crossref.org/servlet/getForwardLinks?pid=rob@terminological.co.uk&doi=10.5555/12345678&include_postedcontent=true
	// https://doi.crossref.org/servlet/getForwardLinks?usr=username&pwd=password&doi=doi&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
	// https://doi.crossref.org/servlet/getForwardLinks?usr=username&pwd=password&doi=prefix&date=YYYY-MM-DD

	// https://doi.crossref.org/servlet/query?pid=rob@terminological.co.uk&id=10.1577/H02-043

	// http://support.crossref.org/hc/en-us/articles/213420726
	// TODO: get pdf using content negotiation and a PdfFetcher...
	
	private static final Logger logger = LoggerFactory.getLogger(CrossRefClient.class);

	private static Map<String,CrossRefClient> singleton = new HashMap<>();

	private CrossRefClient(String developerEmail, Optional<Path> optional) {
		super(optional, 
				TokenBuckets.builder()
				.withCapacity(50)
				.withInitialTokens(50)
				.withFixedIntervalRefillStrategy(50, 1, TimeUnit.SECONDS).build());
		this.developerEmail = developerEmail;
	}

	private String developerEmail;
	private ObjectMapper objectMapper = new ObjectMapper();

	public static CrossRefClient create(String developerEmail) {
		return create(developerEmail, null);
	}

	public static CrossRefClient create(String developerEmail, Path cacheDir) {
		if (singleton.containsKey(developerEmail)) return singleton.get(developerEmail);
		CrossRefClient tmp = new CrossRefClient(developerEmail, Optional.ofNullable(cacheDir));
		singleton.put(developerEmail, tmp);
		return tmp;
	}
	
	protected MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("mailto", developerEmail);
		return out;
	}

	

	static String baseUrl ="https://api.crossref.org/";

	public Optional<SingleResult> getByDoi(String doi) throws BibliographicApiException {
		
		String url = baseUrl+"works/"+encode(doi);
		return this
			.buildCall(url,SingleResult.class)
			.cacheForever()
			.withOperation(is -> new SingleResult(objectMapper.readTree(is)))
			.get();
		
	}

	/*blic ListResult getByQuery(QueryBuilder qb) throws BibliographicApiException, NoSuchElementException {
		
		String key = qb.toString();
		InputStream is;
		if (this.weekCache().containsKey(key)) {
			logger.debug("Cached crossref record for:" + key);
			is = this.weekCache().get(key).inputStream();
		} else {
			rateLimit();
			logger.debug("Querying crossref: "+qb.toString());
			ClientResponse r = qb.get(client).get(ClientResponse.class);
			updateRateLimits(r.getHeaders());
			BinaryData data = BinaryData.from(r.getEntityInputStream());
			this.weekCache().put(key, data);
			is = data.inputStream();
		}
		try {
			CrossRefResult.ListResult  response = ;
			//Check to see if the result is past the end of the set.
			if (
					response.message.isPresent() && 
					response.message.get().items.size() == 0 && 
					response.message.get().totalResults.orElse(0) > 0) {
				throw new NoSuchElementException();
			}
			return response;
		} catch (IOException e) {
			this.weekCache().remove(key);
			throw new BibliographicApiException("Malformed response to: "+qb.get(client).getURI());
		}
	}*/

	public InputStream getTDM(CrossRefResult.Work work, Predicate<String> licenceFilter, String clickThroughToken) throws BibliographicApiException {

		logger.debug("Retrieving crossref content for:" + work.DOI+": "+work.title.get(0));

		if (work.license.stream().map(l -> l.URL.toString()).anyMatch(licenceFilter)) {

			Optional<URL> url = work.link.stream()
					.filter(rl -> rl.intendedApplication.orElse("").equals("text-mining"))
					.flatMap(rl -> rl.URL.stream())
					.findFirst();
			if (!url.isPresent()) throw new BibliographicApiException("no content for intended application of text-mining");

			//TODO: try and find correct type for output, or specify xml. 
			//TODO: implement caching

			WebResource tdmCopy = client.resource(url.get().toString());
			tdmCopy.header("CR-Clickthrough-Client-Token", clickThroughToken);
			ClientResponse r = tdmCopy.get(ClientResponse.class);
			return r.getEntityInputStream();

		} else {
			throw new BibliographicApiException("no licensed content found");
		}

	}

	//TODO:
	/*
	 * curl -LH "Accept: application/x-bibtex" http://dx.doi.org/10.5555/12345678
	 * gets the full doi result in bibtex
	 */
	
	
	public Optional<Work> findWorkByCitationString(String citation) {
		Optional<ListResult> lr = this.buildQuery()
				.withSearchTerm(Field.BIBLIOGRAPHIC, citation)
				.sortedBy(Sort.SCORE, SortOrder.DESC)
				.limit(1)
				.execute();
		Optional<Work> out = lr.stream().map(l -> l.getMessage())
				.flatMap(i -> i.items.stream())
				.filter(o -> o.score.orElse(0F) > 85.0F)
				.findFirst();
		return out;
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

		/*ivate WebResource get(Client client) {
			WebResource tdmCopy = client.resource(url);
			tdmCopy = tdmCopy.queryParams(params);
			return tdmCopy;
		}*/

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

		public QueryBuilder limit(Integer rows) {
			params.add("rows",rows.toString());
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
					resp.getMessage().getNextCursor().isPresent() &&
					!(
							resp.getMessage().getItems().findFirst().isPresent() && 
							resp.getMessage().getTotalResults().orElse(0L) > 0
							)
					) {
				params.remove("cursor");
				params.add("cursor", resp.getMessage().getNextCursor().get());
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

		public Optional<ListResult> execute() {
			return client.buildCall(url, ListResult.class)
					.withParams(params)
					.withOperation(is -> new ListResult(client.objectMapper.readTree(is)))
					.get();
		}

		public String toString() {
			return keyFromApiQuery(url,params); 
		}
	}

	
	public static Predicate<String> ACCEPT_ANY_LICENCE = new Predicate<String>() {
		@Override
		public boolean test(String t) {
			return true;
		}
	};

	public static Predicate<String> ACCEPT_CREATIVE_COMMONS = new Predicate<String>() {
		@Override
		public boolean test(String t) {
			return t.startsWith("http://creativecommons.org");
		}
	};
	
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
