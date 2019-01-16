package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedMap;

import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UnpaywallClient extends CachingApiClient {

	//api.unpaywall.org/v2/DOI?email=YOUR_EMAIL.
	//https://unpaywall.org/YOUR_DOI

	private static final Logger logger = LoggerFactory.getLogger(UnpaywallClient.class);

	private String developerEmail;
	private ObjectMapper objectMapper = new ObjectMapper();
	private PdfFetcher pdfFetcher=null;

	private PdfFetcher getPdfFetcher() {
		if (pdfFetcher==null) pdfFetcher = PdfFetcher.create(cache.resolve("pdf"));
		return pdfFetcher;
	}
	
	private Path cache = null;
	private static HashMap<String, UnpaywallClient> singleton = new HashMap<>();

	public static UnpaywallClient create(String developerEmail) {
		return create(developerEmail,null);
	}
	
	public UnpaywallClient withPdfFetcher(PdfFetcher fetcher) {
		this.pdfFetcher = fetcher;
		return this;
	}
	
	public static UnpaywallClient create(String developerEmail, Path cachePath) {
		if (!singleton.containsKey(developerEmail)) {
			UnpaywallClient tmp = new UnpaywallClient(developerEmail, Optional.ofNullable(cachePath));
			singleton.put(developerEmail, tmp);
		}
		return singleton.get(developerEmail);
	}
	
	public UnpaywallClient withCache(Path cache) {
		this.cache = cache;
		return this;
	}
	
	
	private UnpaywallClient(String developerEmail, Optional<Path> cachePath) {
		super(cachePath, TokenBuckets.builder().withInitialTokens(1000).withCapacity(1000).withFixedIntervalRefillStrategy(1000, 24*6*6, TimeUnit.SECONDS).build());
		this.developerEmail = developerEmail;
		
	}

	protected MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("email", developerEmail);
		return out;
	}

	public Optional<Result> getUnpaywallByDoi(String doi) {
		logger.debug("fetching unpaywall record for: {}",doi);
		return this.buildCall("https://api.unpaywall.org/v2/"+encode(doi), Result.class)
			.cacheForever()
			.withOperation(is -> new Result(objectMapper.readTree(is)))
			.get();
	}

	public Set<Result> getUnpaywallByDois(Collection<String> dois) {
		return dois.stream().flatMap(doi -> getUnpaywallByDoi(doi).stream()).collect(Collectors.toSet());
	}
	
	public Optional<InputStream> getPdfByResult(Result result) {
		try {
			String url = result.getPdfUri().orElseThrow(() -> new BibliographicApiException("no pdf for doi: "+result.getDoi())).toString();
			return getPdfFetcher().getPdfFromUrl(url);
		} catch (Exception e) {
			logger.debug("Cannot fetch content for {} - {}",result.getDoi(), e.getLocalizedMessage());
			return Optional.empty();
		}
	}

	public Optional<InputStream> getPdfByDoi(String doi) {
		Optional<Result> result = getUnpaywallByDoi(doi);
		return result.flatMap(r -> getPdfByResult(r));
	}
	
	public static class Result extends ExtensibleJson {
		public Result(JsonNode node) {super(node);}
		
		public String getDoi() {return this.asString("doi").get();}
 		public String getTitle() {return this.streamPath("title").findFirst().map(
 				n -> n.asString()).orElse(getJournal().orElse("No title"));}
 		public String getFirstAuthorName() {
 			return this.getAuthors().findFirst().map(o -> o.getLastName()).orElse("n/a");
 		}
 		public Optional<String> getJournal() {return this.asString("journal_name");}
 		//public Optional<String> getVolume() {return Optional.empty();}
 		//public Optional<String> getIssue() {return Optional.empty();}
 		public Optional<Long> getYear() {return getDate().map(d -> (long) d.getYear());}
 		//public Optional<String> getPage() {return Optional.empty();}
		
 		public Optional<LocalDate> getDate() {
 			return this.asString("published_date").map(LocalDate::parse);
 		}
 		
 		public Stream<String> getLicenses() {
 			return this.streamPath("oa_locations","URL").map(o -> o.asString());}
 		
 		
 		
 		//public Optional<Double> getScore() {return this.asDouble("score");}
		//public Stream<String> getLicenses() {return this.streamPath("license","URL").map(o -> o.asString());}
		
 		public Stream<Author> getAuthors() {return this.streamPath(Author.class, "z_authors");}
 		//public Stream<Reference> getReferences() {return this.streamPath(Reference.class, "reference");}
		//public Optional<Long> getCitedByCount() {return this.asLong("is-referenced-by-count");}
		//public Long getReferencesCount() {return this.asLong("references-count").get();}
 		public Optional<String> getAbstract() {return this.asString("abstract");}
 		
 		public Optional<URI> getTextMiningUri() {
 			return Optional.empty();
 		}
 		
 		public Optional<URI> getPdfUri() {
			return this.streamPath("best_oa_location","url_for_pdf").findFirst()
					.or(() -> this.streamPath("oa_locations","url_for_pdf").findFirst())
					.map(n -> URI.create(n.asString()));
		}
		
		
		/*@JsonProperty("best_oa_location") public Optional<Location> bestOaLocation = Optional.empty(); //The best OA Location Object we could find for this DOI.
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
		//@JsonProperty("title") public Optional<String> title = Optional.empty(); //The title of this resource.
		@JsonProperty("updated") public Optional<Date> updated = Optional.empty(); //Time when the data for this resource was last updated.
		@JsonProperty("year") public Optional<String> year = Optional.empty(); //The year this resource was published.
		@JsonProperty("z_authors") public List<Author> zAuthors = Collections.emptyList(); //The authors of this resource.
		*/
	}

	/*public static class Location extends ExtensibleJson {
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
	}*/

	public static class Author extends ExtensibleJson implements uk.co.terminological.pubmedclient.record.Author {
		/*@JsonProperty("family") public Optional<String> family = Optional.empty();
		@JsonProperty("given") public Optional<String> given = Optional.empty();
		@JsonProperty("name") public Optional<String> name = Optional.empty();
		@JsonProperty("ORCID") public Optional<String> orchid = Optional.empty();
		@JsonProperty("authenticated-orcid") public Optional<Boolean> authenticated = Optional.empty();
		@JsonProperty("affiliation") public List<Organisation> affiliation = Collections.emptyList();
		*/
		public Author(JsonNode node) { super(node); }
		
		public String getLastName() {return this.asString("family").orElse(this.asString("name").orElse("Unknown"));}
		public Optional<String> getORCID() {return this.asString("ORCID");}
		public Stream<String> getAffiliations() {return this.streamPath("affiliation","name").map(n -> n.asString());}
		public Optional<String> getFirstName() {return this.asString("given");}
		
		public boolean isFirst() {return this.asString("sequence").filter(s -> s.equals("first")).isPresent();}
		
		public String getLabel() {
			return (getLastName()+", "+getFirstName().orElse("Unknown").substring(0, 1)).toLowerCase();
		}

		@Override
		public Optional<String> getInitials() {
			return getFirstName().map(s -> s.substring(0,1));
		}

		
	}
	
	/*public static class Organisation extends ExtensibleJson {
		@JsonProperty("name") public Optional<String> name = Optional.empty();
	}*/
	

}
