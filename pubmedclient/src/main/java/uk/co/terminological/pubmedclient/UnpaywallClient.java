package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.util.concurrent.RateLimiter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UnpaywallClient {

	//api.unpaywall.org/v2/DOI?email=YOUR_EMAIL.
	//https://unpaywall.org/YOUR_DOI

	private static final Logger logger = LoggerFactory.getLogger(UnpaywallClient.class);

	private String developerEmail;
	private Client client;
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	private RateLimiter rateLimiter = RateLimiter.create(100000/24/60/60);
	private static HashMap<String, UnpaywallClient> singleton = new HashMap<>();

	public static UnpaywallClient create(String developerEmail) {
		if (!singleton.containsKey(developerEmail)) {
			UnpaywallClient tmp = new UnpaywallClient(developerEmail);
			singleton.put(developerEmail, tmp);
		}
		return singleton.get(developerEmail);

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

	public InputStream getPreferredContentByDoi(String doi) throws BibliographicApiException {
		try {
			WebResource wr = client.resource("https://api.unpaywall.org/v2/"+encode(doi));
			return wr.get(InputStream.class);		
		} catch (Exception e) {
			throw new BibliographicApiException("Cannot fetch content for "+doi,e);
		}
	}

	public Result getUnpaywallByDoi(String doi) throws BibliographicApiException {
		return getUnpaywallByDois(Collections.singletonList(doi)).stream()
				.findFirst().orElseThrow(() -> new BibliographicApiException("No unpaywall result for: "+doi));
	}

	public List<Result> getUnpaywallByDois(List<String> dois) throws BibliographicApiException {
		List<Result> out = new ArrayList<>();
		dois.forEach(i -> {
			rateLimiter.acquire();
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
			WebResource wr = client.resource(result.pdfUrl().orElseThrow(() -> new BibliographicApiException("No PDF found for "+result.doi.get())));
			return wr.get(InputStream.class);
		} catch (Exception e) {
			throw new BibliographicApiException("Cannot fetch content for "+result.doi.get());
		}
	}

	private Result doCall(String doi) throws BibliographicApiException {
		MultivaluedMap<String, String> params = defaultApiParams();
		WebResource wr = client.resource("https://api.unpaywall.org/v2/"+encode(doi)).queryParams(params);
		try {
			InputStream is = wr.get(InputStream.class); 
			Result response = objectMapper.readValue(is, Result.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			e.printStackTrace();
			throw new BibliographicApiException("Malformed response");
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect");
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
		@JsonProperty("best_oa_location") public Optional<Location> bestOaLocation; //The best OA Location Object we could find for this DOI.
		@JsonProperty("data_standard") public Optional<Integer> dataStandard; //Indicates the data collection approaches used for this resource.
		@JsonProperty("doi") public Optional<String> doi; //The DOI of this resource.
		@JsonProperty("doi_url") public Optional<String> doiUrl; //The DOI in hyperlink form.
		@JsonProperty("genre") public Optional<String> genre; //The type of resource.
		@JsonProperty("is_oa") public Optional<Boolean> isOa; //True if there is an OA copy of this resource.
		@JsonProperty("journal_is_in_doaj") public Optional<Boolean> journalIsInDoaj; //Is this resource published in a DOAJ-indexed journal.
		@JsonProperty("journal_is_oa") public Optional<Boolean> journalIsOa; //Is this resource published in a completely OA journal.
		@JsonProperty("journal_issns") public Optional<String> journalIssns; //Any ISSNs assigned to the journal publishing this resource.
		@JsonProperty("journal_name") public Optional<String> journalName; //The name of the journal publishing this resource.
		@JsonProperty("oa_locations") public List<Location> oaLocations; //List of all the OA Location objects associated with this resource.
		@JsonProperty("published_date") public Optional<Date> publishedDate; //The date this resource was published.
		@JsonProperty("publisher") public Optional<String> publisher; //The name of this resource's publisher.
		@JsonProperty("title") public Optional<String> title; //The title of this resource.
		@JsonProperty("updated") public Optional<Date> updated; //Time when the data for this resource was last updated.
		@JsonProperty("year") public Optional<String> year; //The year this resource was published.
		@JsonProperty("z_authors") public List<Author> zAuthors; //The authors of this resource.
		public Optional<String> pdfUrl() {
			if (bestOaLocation.get().urlForPdf.isPresent())
				return bestOaLocation.get().urlForPdf;
			return oaLocations.stream().flatMap(loc -> loc.urlForPdf.stream()).findFirst();
		}
	}

	public static class Location extends ExtensibleJson {
		@JsonProperty("evidence") public Optional<String> evidence; //How we found this OA location.
		@JsonProperty("host_type") public Optional<String> hostType; //The type of host that serves this OA location.
		@JsonProperty("is_best") public Optional<Boolean> isBest; //Is this location the bestOaLocation for its resource.See the DOI object's bestOaLocation description for more on how we select which location is "best."
		@JsonProperty("license") public Optional<String> license; //The license under which this copy is published.
		@JsonProperty("pmh_id") public Optional<String> pmhId; //OAI-PMH endpoint where we found this location.This is primarily for internal debugging. It's Null for locations that weren't found using OAI-PMH.
		@JsonProperty("updated") public Optional<String> updated; //Time when the data for this location was last updated.Returned as an ISO8601-formatted timestamp. Example: 2017-08-17T23:43:27.753663
		@JsonProperty("url") public Optional<String> url; //The urlForPdf if there is one; otherwise landing page URL.
		@JsonProperty("url_for_landing_page") public Optional<String> urlForLandingPage; //The URL for a landing page describing this OA copy.
		@JsonProperty("url_for_pdf") public Optional<String> urlForPdf; //The URL with a PDF version of this OA copy.
		@JsonProperty("version") public Optional<String> version; //The content version accessible at this location.
	}

	public static class Author extends ExtensibleJson {
		@JsonProperty("family") public Optional<String> family;
		@JsonProperty("given") public Optional<String> given;
	}

}
