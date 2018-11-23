package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UnpaywallClient {

	//api.unpaywall.org/v2/DOI?email=YOUR_EMAIL.
	//https://unpaywall.org/YOUR_DOI

	private static final Logger logger = LoggerFactory.getLogger(UnpaywallClient.class);

	private String developerEmail;
	private Client client;
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	private TokenBucket rateLimiter = TokenBuckets.builder().withInitialTokens(1000).withCapacity(1000).withFixedIntervalRefillStrategy(1000, 24*6*6, TimeUnit.SECONDS).build();
	private static HashMap<String, UnpaywallClient> singleton = new HashMap<>();

	public static UnpaywallClient create(String developerEmail) {
		if (!singleton.containsKey(developerEmail)) {
			UnpaywallClient tmp = new UnpaywallClient(developerEmail);
			singleton.put(developerEmail, tmp);
		}
		return singleton.get(developerEmail);

	}
	
	public UnpaywallClient debugMode() {
		this.client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
			@Override public void info(String msg) { logger.info(msg); }
		}));
		return this;
	}

	private UnpaywallClient(String developerEmail) {
		this.developerEmail = developerEmail;
		this.client = Client.create();
	}

	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("email", developerEmail);
		return out;
	}

	public InputStream getPreferredContentByDoi(String doi, Path cacheDir) throws BibliographicApiException {
		Path filepath = cacheDir.resolve(doi+".pdf");
		try {
			if (!Files.exists(filepath)) {
				Files.createDirectories(filepath.getParent());
				Files.copy(
						this.getPreferredContentByDoi(doi),
						filepath);
			}
			return Files.newInputStream(filepath);
		} catch (IOException e) {
			throw new BibliographicApiException("Could not get content for"+doi,e);
		}
	}
	
	public InputStream getPreferredContentByDoi(String doi) throws BibliographicApiException {
		try {
			Result tmp = getUnpaywallByDoi(doi);
			WebResource wr = client.resource(tmp.pdfUrl().orElseThrow(() -> new BibliographicApiException("No paywall result for: "+doi)));
			return wr.get(InputStream.class);		
		} catch (Exception e) {
			throw new BibliographicApiException("Cannot fetch content for "+doi,e);
		}
	}

	public Result getUnpaywallByDoi(String doi) throws BibliographicApiException {
		return getUnpaywallByDois(Collections.singletonList(doi)).stream()
				.findFirst().orElseThrow(() -> new BibliographicApiException("No unpaywall result for: "+doi));
	}

	public Set<Result> getUnpaywallByDois(Collection<String> dois) throws BibliographicApiException {
		Set<Result> out = new HashSet<>();
		dois.forEach(i -> {
			try {
				out.add(doCall(i));
			} catch (BibliographicApiException e) {
				logger.debug("could not get unpaywall record for "+i);
			}
		});
		return out;
	}

	public InputStream getPdfByResult(Result result) throws BibliographicApiException {
		try {
			
			String url = result.pdfUrl().orElseThrow(() -> new BibliographicApiException("No PDF found for "+result.doi.get()));
			return PdfUtil.getPdfFromUrl(url);
		    
		} catch (Exception e) {
			throw new BibliographicApiException("Cannot fetch content for "+result.doi.get(), e);
		}
	}

	private Result doCall(String doi) throws BibliographicApiException {
		MultivaluedMap<String, String> params = defaultApiParams();
		logger.debug("https://api.unpaywall.org/v2/"+encode(doi));
		rateLimiter.consume();
		WebResource wr = client.resource("https://api.unpaywall.org/v2/"+encode(doi)).queryParams(params);
		try {
			InputStream is = wr.get(InputStream.class); 
			Result response = objectMapper.readValue(is, Result.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			throw new BibliographicApiException("Malformed response");
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect or not found");
		}
	}


	private static String encode(String string)  {
		try {
			return URLEncoder.encode(string,java.nio.charset.StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static class Result extends ExtensibleJson {
		@JsonProperty("best_oa_location") public Optional<Location> bestOaLocation = Optional.empty(); //The best OA Location Object we could find for this DOI.
		@JsonProperty("data_standard") public Optional<Integer> dataStandard = Optional.empty(); //Indicates the data collection approaches used for this resource.
		@JsonProperty("doi") public Optional<String> doi = Optional.empty(); //The DOI of this resource.
		@JsonProperty("doi_url") public Optional<String> doiUrl = Optional.empty(); //The DOI in hyperlink form.
		@JsonProperty("genre") public Optional<String> genre = Optional.empty(); //The type of resource.
		@JsonProperty("is_oa") public Optional<Boolean> isOa = Optional.empty(); //True if there is an OA copy of this resource.
		@JsonProperty("journal_is_in_doaj") public Optional<Boolean> journalIsInDoaj = Optional.empty(); //Is this resource published in a DOAJ-indexed journal.
		@JsonProperty("journal_is_oa") public Optional<Boolean> journalIsOa = Optional.empty(); //Is this resource published in a completely OA journal.
		@JsonProperty("journal_issns") public Optional<String> journalIssns = Optional.empty(); //Any ISSNs assigned to the journal publishing this resource.
		@JsonProperty("journal_name") public Optional<String> journalName = Optional.empty(); //The name of the journal publishing this resource.
		@JsonProperty("oa_locations") public List<Location> oaLocations = Collections.emptyList(); //List of all the OA Location objects associated with this resource.
		@JsonProperty("published_date") public Optional<Date> publishedDate = Optional.empty(); //The date this resource was published.
		@JsonProperty("publisher") public Optional<String> publisher = Optional.empty(); //The name of this resource's publisher.
		@JsonProperty("title") public Optional<String> title = Optional.empty(); //The title of this resource.
		@JsonProperty("updated") public Optional<Date> updated = Optional.empty(); //Time when the data for this resource was last updated.
		@JsonProperty("year") public Optional<String> year = Optional.empty(); //The year this resource was published.
		@JsonProperty("z_authors") public List<Author> zAuthors = Collections.emptyList(); //The authors of this resource.
		public Optional<String> pdfUrl() {
			if (bestOaLocation.isPresent() && bestOaLocation.get().urlForPdf.isPresent())
				return bestOaLocation.get().urlForPdf;
			return oaLocations.stream().flatMap(loc -> loc.urlForPdf.stream()).findFirst();
		}
		public Optional<LocalDate> getPublishedDate() {
			return publishedDate.map(o -> o.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
		}
	}

	public static class Location extends ExtensibleJson {
		@JsonProperty("evidence") public Optional<String> evidence = Optional.empty(); //How we found this OA location.
		@JsonProperty("host_type") public Optional<String> hostType = Optional.empty(); //The type of host that serves this OA location.
		@JsonProperty("is_best") public Optional<Boolean> isBest = Optional.empty(); //Is this location the bestOaLocation for its resource.See the DOI object's bestOaLocation description for more on how we select which location is "best."
		@JsonProperty("license") public Optional<String> license = Optional.empty(); //The license under which this copy is published.
		@JsonProperty("pmh_id") public Optional<String> pmhId = Optional.empty(); //OAI-PMH endpoint where we found this location.This is primarily for internal debugging. It's Null for locations that weren't found using OAI-PMH.
		@JsonProperty("updated") public Optional<String> updated = Optional.empty(); //Time when the data for this location was last updated.Returned as an ISO8601-formatted timestamp. Example: 2017-08-17T23:43:27.753663
		@JsonProperty("url") public Optional<String> url = Optional.empty(); //The urlForPdf if there is one; otherwise landing page URL.
		@JsonProperty("url_for_landing_page") public Optional<String> urlForLandingPage = Optional.empty(); //The URL for a landing page describing this OA copy.
		@JsonProperty("url_for_pdf") public Optional<String> urlForPdf = Optional.empty(); //The URL with a PDF version of this OA copy.
		@JsonProperty("version") public Optional<String> version = Optional.empty(); //The content version accessible at this location.
	}

	public static class Author extends ExtensibleJson {
		@JsonProperty("family") public Optional<String> family = Optional.empty();
		@JsonProperty("given") public Optional<String> given = Optional.empty();
		@JsonProperty("name") public Optional<String> name = Optional.empty();
		@JsonProperty("ORCID") public Optional<String> orchid = Optional.empty();
		@JsonProperty("authenticated-orcid") public Optional<Boolean> authenticated = Optional.empty();
		@JsonProperty("affiliation") public List<Organisation> affiliation = Collections.emptyList();
	}
	
	public static class Organisation extends ExtensibleJson {
		@JsonProperty("name") public Optional<String> name = Optional.empty();
	}
	

}
