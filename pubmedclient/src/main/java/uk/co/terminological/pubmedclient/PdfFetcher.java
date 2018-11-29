package uk.co.terminological.pubmedclient;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class PdfFetcher extends CachingApiClient {

	static Logger logger = LoggerFactory.getLogger(PdfFetcher.class);
	
	static PdfFetcher instance = null;
	
	int maxRedirects = 10;
	Path cache = null;
	
	private PdfFetcher(Optional<Path> cachePath) {
		super(cachePath, TokenBuckets.builder().withInitialTokens(10).withCapacity(10).withFixedIntervalRefillStrategy(10, 1, TimeUnit.SECONDS).build());
		ClientConfig config = new DefaultClientConfig();
	    config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
	    client = Client.create(config);
	    client.setFollowRedirects(true);
	    client.addFilter(new ClientFilter() { 
            @Override 
            public ClientResponse handle(ClientRequest request) 
                            throws ClientHandlerException { 
                    request.getHeaders().add( 
                                    HttpHeaders.USER_AGENT, 
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.119 Safari/537.36"); 
                    return getNext().handle(request); 
            } 
	    });
	    client.addFilter(new ClientFilter() {
	    	@Override
	        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
	            ClientHandler ch = getNext();
	            int i=maxRedirects;
	            while (i-->0) {
		            ClientResponse resp = ch.handle(cr);
		            if (resp.getClientResponseStatus().getFamily() != Response.Status.Family.REDIRECTION) {
		                return resp;
		            } else {
		                // try location
		                String redirectTarget = resp.getHeaders().getFirst("Location");
		                logger.debug("redirecting to :"+redirectTarget);
		                cr.setURI(UriBuilder.fromUri(redirectTarget).build());
		                resp = ch.handle(cr);
		            }
	            }
	            throw new ClientHandlerException("Too many redirects");
	        }
	    });
	}
	
	public static PdfFetcher create() {
		return new PdfFetcher(Optional.empty());
	}
	
	public static PdfFetcher create(Path cachePath) {
		if (instance == null) instance = new PdfFetcher(Optional.ofNullable(cachePath));
		return instance;
	}
	
	public PdfFetcher maxRedirects(int redirects) {
		this.maxRedirects = redirects;
		return this;
	}
	
	public Optional<InputStream> getPdfFromUrl(String url) {
		return this.buildCall(url, InputStream.class)
			.cacheForever()
			.withOperation(is -> is)
			.get();
	}

@Override
	protected MultivaluedMap<String, String> defaultApiParams() {
		return new MultivaluedMapImpl();
	}
	
	
}
