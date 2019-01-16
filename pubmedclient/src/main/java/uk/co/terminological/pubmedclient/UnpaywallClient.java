package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
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

import uk.co.terminological.pubmedclient.record.IdType;
import uk.co.terminological.pubmedclient.record.RecordReference;

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
			String url = result.getPdfUri().orElseThrow(() -> new BibliographicApiException("no pdf for doi: "+result.getIdentifier())).toString();
			return getPdfFetcher().getPdfFromUrl(url);
		} catch (Exception e) {
			logger.debug("Cannot fetch content for {} - {}",result.getIdentifier(), e.getLocalizedMessage());
			return Optional.empty();
		}
	}

	public Optional<InputStream> getPdfByDoi(String doi) {
		Optional<Result> result = getUnpaywallByDoi(doi);
		return result.flatMap(r -> getPdfByResult(r));
	}
	
	public static class Result extends ExtensibleJson implements uk.co.terminological.pubmedclient.record.Record {
		public Result(JsonNode node) {super(node);}
		
		public String getIdentifier() {return this.asString("doi").get();}
 		public String getTitle() {return this.streamPath("title").findFirst().map(
 				n -> n.asString()).orElse(getJournal());}
 		public String getFirstAuthorName() {
 			return this.getAuthors().findFirst().map(o -> o.getLastName()).orElse("n/a");
 		}
 		public String getJournal() {return this.asString("journal_name").orElse("Unknown");}
 		public Optional<Long> getYear() {return getDate().map(d -> (long) d.getYear());}
 		
 		public Optional<LocalDate> getDate() {
 			return this.asString("published_date").map(LocalDate::parse);
 		}
 		
 		public Stream<String> getLicenses() {
 			return this.streamPath("oa_locations","URL").map(o -> o.asString());}
 		
 		//public Optional<Double> getScore() {return this.asDouble("score");}
		
 		public Stream<Author> getAuthors() {return this.streamPath(Author.class, "z_authors");}
 		public Optional<String> getAbstract() {return this.asString("abstract");}
 		
 		public Optional<URI> getTextMiningUri() {
 			return Optional.empty();
 		}
 		
 		public Optional<URI> getPdfUri() {
			return this.streamPath("best_oa_location","url_for_pdf").findFirst()
					.or(() -> this.streamPath("oa_locations","url_for_pdf").findFirst())
					.map(n -> URI.create(n.asString()));
		}

		@Override
		public IdType getIdentifierType() {
			return IdType.DOI;
		}

		@Override
		public Set<RecordReference> getOtherIdentifiers() {
			return Collections.emptySet();
		}
		
	}

	public static class Author extends ExtensibleJson implements uk.co.terminological.pubmedclient.record.Author {
		
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
	

}
