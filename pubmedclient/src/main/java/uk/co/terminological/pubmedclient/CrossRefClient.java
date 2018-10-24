package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class CrossRefClient {
	// https://www.crossref.org/schemas/
	// https://github.com/CrossRef/rest-api-doc/blob/master/api_format.md
	// https://github.com/CrossRef/rest-api-doc
	// http://tdmsupport.crossref.org/researchers/
	// http://clickthroughsupport.crossref.org/
	
	private String clickThroughToken;
	private String developerEmail;
	private Client client;
	private Integer rateLimitRequests;
	private Integer rateLimitInterval;
	
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
	
}
