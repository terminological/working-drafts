package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private ObjectMapper objectMapper = new ObjectMapper();
	static RateLimiter rateLimiter = RateLimiter.create(100000);
	
	public UnpaywallClient(String developerEmail) {
		this.developerEmail = developerEmail;
		this.client = Client.create();
	}
	
	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("email", developerEmail);
		return out;
	}
	
	public InputStream getContent(String doi) throws BibliographicApiException {
		try {
			WebResource wr = client.resource("https://api.unpaywall.org/v2/"+encode(doi));
			return wr.get(InputStream.class);		
		} catch (Exception e) {
			throw new BibliographicApiException("Cannot fetch content for "+doi,e);
		}
	}
	
	public Result getUnpaywall(String doi) throws BibliographicApiException {
		return getUnpaywall(Collections.singletonList(doi)).stream()
				.findFirst().orElseThrow(() -> new BibliographicApiException("No unpaywall result for: "+doi));
	}
	
	public List<Result> getUnpaywall(List<String> dois) throws BibliographicApiException {
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
	
	public static class Result {
		@JsonProperty("best_oa_location") public Location bestOaLocation; //The best OA Location Object we could find for this DOI.
		@JsonProperty("data_standard") public Integer dataStandard; //Indicates the data collection approaches used for this resource.
		@JsonProperty("doi") public String doi; //The DOI of this resource.
		@JsonProperty("doi_url") public String doiUrl; //The DOI in hyperlink form.
		@JsonProperty("genre") public String genre; //The type of resource.
		@JsonProperty("is_oa") public Boolean isOa; //True if there is an OA copy of this resource.
		@JsonProperty("journal_is_in_doaj") public Boolean journalIsInDoaj; //Is this resource published in a DOAJ-indexed journal.
		@JsonProperty("journal_is_oa") public Boolean journalIsOa; //Is this resource published in a completely OA journal.
		@JsonProperty("journal_issns") public String journalIssns; //Any ISSNs assigned to the journal publishing this resource.
		@JsonProperty("journal_name") public String journalName; //The name of the journal publishing this resource.
		@JsonProperty("oa_locations") public List<Location> oaLocations; //List of all the OA Location objects associated with this resource.
		@JsonProperty("published_date") public Date publishedDate; //The date this resource was published.
		@JsonProperty("publisher") public String publisher; //The name of this resource's publisher.
		@JsonProperty("title") public String title; //The title of this resource.
		@JsonProperty("updated") public Date updated; //Time when the data for this resource was last updated.
		@JsonProperty("year") public String year; //The year this resource was published.
		@JsonProperty("z_authors") public List<Author> zAuthors; //The authors of this resource.
	}
	
	public static class Location {
		@JsonProperty("evidence") public String evidence; //How we found this OA location.
		@JsonProperty("host_type") public String hostType; //The type of host that serves this OA location.
		@JsonProperty("is_best") public Boolean isBest; //Is this location the bestOaLocation for its resource.See the DOI object's bestOaLocation description for more on how we select which location is "best."
		@JsonProperty("license") public String license; //The license under which this copy is published.
		@JsonProperty("pmh_id") public String pmhId; //OAI-PMH endpoint where we found this location.This is primarily for internal debugging. It's Null for locations that weren't found using OAI-PMH.
		@JsonProperty("updated") public String updated; //Time when the data for this location was last updated.Returned as an ISO8601-formatted timestamp. Example: 2017-08-17T23:43:27.753663
		@JsonProperty("url") public String url; //The urlForPdf if there is one; otherwise landing page URL.
		@JsonProperty("url_for_landing_page") public String urlForLandingPage; //The URL for a landing page describing this OA copy.
		@JsonProperty("url_for_pdf") public String urlForPdf; //The URL with a PDF version of this OA copy.
		@JsonProperty("version") public String version; //The content version accessible at this location.
	}

	public static class Author {
		@JsonProperty("family") public String family;
		@JsonProperty("given") public String given;
	}
	
}
