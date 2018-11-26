package uk.co.terminological.pubmedclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.tools.packager.Log;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.pubmedclient.EntrezResult.Links;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

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
	private String baseUrl;
	private static final Logger logger = LoggerFactory.getLogger(EntrezClient.class);
	private static final String ESEARCH = "esearch.fcgi";
	private static final String EFETCH = "efetch.fcgi";
	private static final String ELINK = "elink.fcgi";
	private TokenBucket rateLimiter = TokenBuckets.builder().withInitialTokens(10).withCapacity(10).withFixedIntervalRefillStrategy(10, 1, TimeUnit.SECONDS).build();

	private Path cache = null;
	public static Map<String, EntrezClient> singleton = new HashMap<>();

	public static EntrezClient create(String apiKey, String appId, String developerEmail) {

		if (!singleton.containsKey(apiKey)) {
			EntrezClient tmp = new EntrezClient(DEFAULT_BASE_URL, apiKey, appId, developerEmail);
			singleton.put(apiKey, tmp);
		}
		return singleton.get(apiKey);

	}
	
	public EntrezClient debugMode() {
		this.client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
			@Override public void info(String msg) { logger.info(msg); }
		}));
		return this;
	}

	public EntrezClient withCache(Path cache) {
		this.cache=cache;
		return this;
	}
	
	// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"
	public EntrezClient(String baseUrl, String apiKey, String appId, String developerEmail) {
		this.baseUrl = baseUrl;
		client = Client.create();
		eSearchResource = client.resource(this.baseUrl + ESEARCH);
		eFetchResource = client.resource(this.baseUrl + EFETCH);
		eLinkResource = client.resource(this.baseUrl + ELINK);
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
		
		boolean empty=false;
		EntrezClient client;

		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}
		
		protected <X> X post(WebResource searchService, Class<X> output) {
			WebResource tdmCopy = searchService;
			return tdmCopy.post(output,searchParams);
		}

		protected ESearchQueryBuilder(MultivaluedMap<String, String> searchParams, String searchTerm, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.add("term", searchTerm);
			if (searchTerm.isEmpty()) empty=true;
			this.searchParams.add("db", "pubmed");
			this.searchParams.add("retmax", "100000");
			this.searchParams.add("usehistory", "y");
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
		
		public Optional<EntrezResult.Search> execute() throws BibliographicApiException {
			if (empty) return Optional.empty();
			return Optional.of(client.search(this));
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
		
		try {
			Xml resp = Xml.fromStream(is);
			return new EntrezResult.Search(resp.content());
		} catch (XmlException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		}
		
	}


	public List<String> findPMIdsBySearch(String searchTerm) throws BibliographicApiException {
		if (searchTerm == null || searchTerm.isEmpty()) return Collections.emptyList(); 
		return this.buildSearchQuery(searchTerm).execute().get().getIds().collect(Collectors.toList());
	}

	public List<String> findPMIdsByDoi(String doi) throws BibliographicApiException {
		if (doi.isEmpty()) return Collections.emptyList();
		return this.buildSearchQuery(doi).execute().get().getIds().collect(Collectors.toList());
	}

	public List<String> findPMIdsByPubMedCentralIds(String pmcid) throws BibliographicApiException {
		if (pmcid.isEmpty()) return Collections.emptyList();
		return this.buildSearchQuery("PMC"+pmcid.replace("PMC", "")).execute().get().getIds().collect(Collectors.toList());
	}
	
	public Set<EntrezResult.PubMedEntry> getPMEntriesByPMIds(Collection<String> pmids) throws BibliographicApiException {
		return getPMEntriesByPMIds(pmids, cache);
	}
	
	/**
	 * Fetch PubMed article metadata and abstract
	 * 
	 * @param pmid
	 * @return
	 * @throws BibliographicApiException 
	 * @throws JAXBException
	 */
	public Set<EntrezResult.PubMedEntry> getPMEntriesByPMIds(Collection<String> pmids, Path cache) throws BibliographicApiException {
		Set<EntrezResult.PubMedEntry> out = new HashSet<>();
		if (pmids.isEmpty()) return out;
		Collection<String> deferred = new HashSet<>();
		if (cache != null) {
			for (String pmid: pmids) {
				Path tmp = cache.resolve(pmid);
				if (Files.exists(tmp)) {
					try {
						out.add(new PubMedEntry(Xml.fromFile(tmp.toFile()).content()));
					} catch (XmlException | FileNotFoundException e) {
						logger.debug("error with cached content for: "+pmid);
						deferred.add(pmid);
					}
				} else {
					deferred.add(pmid);
				}
			}
		} else {
			deferred = pmids;
		}
		
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		deferred.forEach(id -> fetchParams.add("id",id));
		fetchParams.add("format", "xml");
		rateLimiter.consume();
		logger.debug("making efetch query with params {}", fetchParams.toString());
		InputStream is = eFetchResource.post(InputStream.class,fetchParams);
		Xml xml;
		try {
			xml = Xml.fromStream(is);
			EntrezResult.PubMedEntries tmp = new EntrezResult.PubMedEntries(xml.content());
			tmp.stream().forEach(entry -> {
				out.add(entry);
				if (cache != null) {
					Path tmp2 = cache.resolve(entry.getPMID().get());
					try {
						entry.getRaw().write(Files.newOutputStream(tmp2));
					} catch (XmlException | IOException e) {
						Log.debug("could not cache: "+entry.getPMID().get());
					}
				}
			});
		} catch (XmlException e) {
			throw new BibliographicApiException("could not parse result",e);
		}
		return out;
	}
	
	public EntrezResult.PubMedEntries getPMEntriesByWebEnvAndQueryKey(String webEnv, String queryKey) throws BibliographicApiException {
		MultivaluedMap<String, String> fetchParams = defaultApiParams();
		fetchParams.add("db", "pubmed");
		fetchParams.add("format", "xml");
		fetchParams.add("query_key", queryKey);
		fetchParams.add("WebEnv", webEnv);
		rateLimiter.consume();
		logger.debug("making efetch query with params {}", fetchParams.toString());
		InputStream is = eFetchResource.post(InputStream.class,fetchParams);
		Xml xml;
		try {
			xml = Xml.fromStream(is);
		} catch (XmlException e) {
			throw new BibliographicApiException("could not parse result",e);
		}
		return new EntrezResult.PubMedEntries(xml.content());
	}

	public Optional<EntrezResult.PubMedEntry> getPMEntryByPMId(String pmid) throws BibliographicApiException {
		if (pmid == null || pmid.isEmpty()) return Optional.empty(); 
		return getPMEntriesByPMIds(Collections.singletonList(pmid)).stream().findFirst();
	}

	/**
	 * retrieves a full text for an article from PubMed Central
	 * @param pmcId
	 * @return
	 * @throws JAXBException
	 */
	public Optional<InputStream> getPubMedCentralXMLByPubMedCentralId(String pmcId) {
		if (pmcId == null || pmcId.isEmpty()) return Optional.empty();
		return getXMLByIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC);
	}


	public Optional<InputStream> getPubMedCentralXMLByPMEntries(EntrezResult.PubMedEntries pmEntries) {
		List<String> pmcIds = pmEntries.stream().flatMap(e -> e.getPMCID().stream()).collect(Collectors.toList());
		return getXMLByIdsAndDatabase(pmcIds, Database.PMC);
	}

	public Optional<InputStream> getPubMedCentralXMLByPMEntry(EntrezResult.PubMedEntry pmEntry) throws BibliographicApiException {
		String pmcId = pmEntry.getPMCID().orElseThrow(() -> new BibliographicApiException("No PMC id for Entry"));
		return getPubMedCentralXMLByPubMedCentralId(pmcId);
	}

	public Optional<InputStream> getPubMedCentralPdfByPMEntry(EntrezResult.PubMedEntry pmEntry) throws BibliographicApiException {
		if (pmEntry.getPMCPdfUrl().isPresent()) {
			String pdfUrl = pmEntry.getPMCPdfUrl().get();
			return Optional.of(PdfUtil.getPdfFromUrl(pdfUrl));
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * retrieves a full entries for a list of articles from the given database 
	 * @param list of ids
	 * @return
	 */
	public Optional<InputStream> getXMLByIdsAndDatabase(Collection<String> ids,Database db) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("db", db.name().toLowerCase());
		params.add("retmode", "xml");
		params.add("id", ids.stream().collect(Collectors.joining(",")));
		logger.debug("making efetch query with params {}", params.toString());
		rateLimiter.consume();
		try {
			return Optional.of(eFetchResource.post(InputStream.class,params));
		} catch (Exception e) {
			return Optional.empty();
		}
	}


	/**
	 * Elinks
	 * @return
	 */
	public ELinksQueryBuilder buildLinksQueryForIdsAndDatabase(Collection<String> ids, Database fromDb) {
		return new ELinksQueryBuilder(defaultApiParams(),ids, fromDb, this);
	}

	/* The history mechanism doesn't really work due to a bug in Entrez
	 * public ELinksQueryBuilder buildLinksQueryForSearchResult(Search search, Database fromDb) {
		return new ELinksQueryBuilder(defaultApiParams(), search.getWebEnv().get(), search.getQueryKey().get(),fromDb, this);
	}*/
	
	public static class ELinksQueryBuilder {
		MultivaluedMap<String, String> searchParams;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		boolean empty = false;
		EntrezClient client;

		@Deprecated
		protected WebResource get(WebResource searchService) {
			WebResource tdmCopy = searchService;
			return tdmCopy.queryParams(searchParams);
		}

		protected <X> X post(WebResource searchService, Class<X> output) {
			WebResource tdmCopy = searchService;
			return tdmCopy.post(output,searchParams);
		}
		
		//TODO: use putSingle instead of add
		protected ELinksQueryBuilder(MultivaluedMap<String, String> searchParams, Collection<String> ids, Database fromDb, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.add("dbfrom", fromDb.name().toLowerCase());
			this.searchParams.add("cmd", "neighbour_score");
			this.searchParams.add("retmode", "xml");
			this.searchParams.add("retmax", "100000");
			ids.forEach(id -> this.searchParams.add("id", id));
			if (ids.isEmpty()) empty=true;
			this.client = client;
		}
		
		protected ELinksQueryBuilder(MultivaluedMap<String, String> searchParams, String webEnv, String queryKey, Database fromDb, EntrezClient client) {
			this.searchParams = searchParams;
			this.searchParams.add("dbfrom", fromDb.name().toLowerCase());
			this.searchParams.add("cmd", "neighbour_score");
			this.searchParams.add("retmode", "xml");
			this.searchParams.add("WebEnv", webEnv);
			this.searchParams.add("retmax", "100000");
			this.searchParams.add("query_key", queryKey);
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
			this.searchParams.putSingle("linkname", linkName);
			return this;
		}

		public ELinksQueryBuilder searchLinked(String term) {
			this.searchParams.putSingle("term", term);
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

		public Optional<Links> execute() throws BibliographicApiException {
			if (empty) return Optional.empty();
			return Optional.of(client.link(this));
		}
		
		public String toString() {return searchParams.toString();}
	}

	public static enum Command {
		NEIGHBOR, NEIGHBOR_SCORE, PRLINKS, LLINKS
	}


	public EntrezResult.Links link(ELinksQueryBuilder builder) throws BibliographicApiException {
		logger.debug("making elink query with params {}", builder.toString());
		rateLimiter.consume();
		InputStream is = builder.post(eLinkResource, InputStream.class);
		try {
			
			Xml resp = Xml.fromStream(is);
			is.close();
			return new EntrezResult.Links(resp.content());
			
		} catch (XmlException | IOException e1) {
			throw new BibliographicApiException("could not parse result",e1);
		} 
		
	}

	public Map<String,Long> getSimilarScoredPMIdsByPMId(String pmid) throws BibliographicApiException {
		Map<String,Long> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmid), Database.PUBMED)
			.command(Command.NEIGHBOR_SCORE)
			.withLinkname("pubmed_pubmed")
			.execute().stream()
			.flatMap(o -> o.stream())
			.forEach(l -> {
				l.toId.ifPresent(to -> out.put(to, l.score.orElse(0L)));
			});
		return out;
	}

	public Map<String,String> getPubMedCentralIdsByPMId(Collection<String> pmids) throws BibliographicApiException {
		Map<String,String> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pmc")
				.execute().stream()
				.flatMap(o -> o.stream())
				.forEach(l -> l.toId.ifPresent(to -> out.put(l.fromId, to)));
		return out;
	}

	public Map<String,String> getReferencedPMIdsByPMId(Collection<String> pmids) throws BibliographicApiException {
		Map<String,String> out = new HashMap<>();
		this.buildLinksQueryForIdsAndDatabase(pmids, Database.PUBMED)
				.toDatabase(Database.PUBMED)
				.command(Command.NEIGHBOR)
				.withLinkname("pubmed_pubmed_refs")
				.execute().stream()
				.flatMap(o -> o.stream())
				.forEach(l -> l.toId.ifPresent(to -> out.put(l.fromId, to)));
		return out;
	}

	public List<String> getReferencingPubMedCentralIdsByPubMedCentralId(String pmcId) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pmc_pmc_citedby")
				.execute().stream()
				.flatMap(o -> o.stream())
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}

	public List<String> getReferencedPubMedCentralIdsByPubMedCentralId(String pmcId) throws BibliographicApiException {
		return this.buildLinksQueryForIdsAndDatabase(Collections.singletonList(pmcId), Database.PMC)
				.toDatabase(Database.PMC)
				.command(Command.NEIGHBOR)
				.withLinkname("pmc_pmc_cites")
				.execute().stream()
				.flatMap(o -> o.stream())
				.flatMap(l -> l.toId.stream())
				.collect(Collectors.toList());
	}
	
}