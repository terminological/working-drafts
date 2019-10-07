package uk.co.terminological.bibliography.europepmc;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.isomorphism.util.TokenBuckets;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.bibliography.CachingApiClient;

public class EuropePmcClient extends CachingApiClient {

	// https://europepmc.org/RestfulWebService
	
	// ####### Constructors / factories etc ####### //
	
	private EuropePmcClient(Optional<Path> optional, String developerEmail) {
		super(optional,
				TokenBuckets.builder()
				.withCapacity(50)
				.withInitialTokens(50)
				.withFixedIntervalRefillStrategy(50, 1, TimeUnit.SECONDS).build());
		this.developerEmail = developerEmail;	
	}
	
	private static String baseUrl = "https://www.ebi.ac.uk/europepmc/webservices/rest/"; //search?query=malaria&format=json

	private String developerEmail;
	
	@Override
	protected MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("format", "json");
		if (developerEmail != null) out.add("email", developerEmail);
		return out;
	}
	
	private static EuropePmcClient singleton;
	
	public static  EuropePmcClient create(String developerEmail) {
		return create(null, developerEmail);
	}
	
	public static  EuropePmcClient create(Path cacheDir, String developerEmail) {
		if (singleton == null) singleton = new EuropePmcClient(Optional.ofNullable(cacheDir), developerEmail);
		return singleton;
	}
	
	// ####### API methods ####### //
	
	public ListResult search(String text) {
		
	}
	
	public List<Citation> citations(String pubmedId) {
		
	}
	
	public List<Citation>
}
