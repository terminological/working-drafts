package uk.co.terminological.bibliography.europepmc;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedMap;

import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.bibliography.CachingApiClient;
import uk.co.terminological.bibliography.record.IdType;

public class EuropePmcClient extends CachingApiClient {

	// https://europepmc.org/RestfulWebService
	
	// ####### Constructors / factories etc ####### //
	
	private static Logger log = LoggerFactory.getLogger(EuropePmcClient.class);
	
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
	
	public Optional<CoreResult> getById(String id, IdType type) {
		ListResult<CoreResult> tmp;
		if( type.equals(IdType.DOI)) {
			tmp = fullSearch("DOI:"+id);
		} else if( type.equals(IdType.PMCID)) {
			tmp = fullSearch("PMCID:"+id);
		} else if( type.equals(IdType.PMID)) {
			//EXT_ID:16199517
			tmp = fullSearch("EXT_ID:"+id);
		} else {
			tmp = fullSearch(id);
		}
		return tmp.getItems().findFirst();
	}
	
	public ListResult<LiteResult> liteSearch(String text) {
		// https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=DOI:10.1038/nature09534&sort=CITED%20desc&format=json
		// https://www.ebi.ac.uk/europepmc/webservices/rest/searchPOST
		// query=malaria%20sort_cited:y
		// format=json
		// sort=CITED desc (P_PDATE_D, AUTH_FIRST, CITED (see https://www.ebi.ac.uk/europepmc/webservices/rest/fields))
		// pageSize=1000 (max)
		// cursorMark=* (paging...)
		// synonym=false/true
		// resultType=idlist/lite/core
		// POST
	}
	
	public ListResult<CoreResult> fullSearch(String text) {
		// https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=DOI:10.1038/nature09534&sort=CITED%20desc&format=json
		// https://www.ebi.ac.uk/europepmc/webservices/rest/searchPOST
		// query=malaria%20sort_cited:y
		// format=json
		// sort=CITED desc (P_PDATE_D, AUTH_FIRST, CITED (see https://www.ebi.ac.uk/europepmc/webservices/rest/fields))
		// pageSize=1000 (max)
		// cursorMark=* (paging...)
		// synonym=false/true
		// resultType=idlist/lite/core
		// POST
	}
	
	public ListResult<Citation> citations(String pubmedId) {
		//https://www.ebi.ac.uk/europepmc/webservices/rest/MED/9843981/citations?format=json
		//id=23245604
		//source=MED
		//page=0
		//pageSize=1000 (max)
		//format=json
		
	}
	
	public ListResult<Reference> references(String pubmedId) {
		//https://www.ebi.ac.uk/europepmc/webservices/rest/MED/9843981/references?format=json
		//id=23245604
		//source=MED
		//page=0
		//pageSize=1000 (max)
		//format=json
		
	}
	
	
}
