package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class PdfFetcher {

	static Logger logger = LoggerFactory.getLogger(PdfFetcher.class);
	
	int maxRedirects = 10;
	boolean debug = false;
	Path cache = null;
	
	private PdfFetcher() {}
	
	public static PdfFetcher create() {
		return new PdfFetcher();
	}
	
	public PdfFetcher debugMode() {
		this.debug = true;
		return this;
	}
	
	public PdfFetcher withCache(Path cache) {
		this.cache = cache;
		return this;
	}
	
	public PdfFetcher maxRedirects(int redirects) {
		this.maxRedirects = redirects;
		return this;
	}
	
	public InputStream getPdfFromUrl(String url, Function<Path,Path> resolve) {
		Path tmp = resolve.apply(cache);
		if (cache == null) return getPdfFromUrl(url);
		
		if (Files.isDirectory(tmp)) {
			logger.error("tried to cache pdf as a directory");
			return getPdfFromUrl(url);
		}
		try {
			if (!Files.exists(tmp)) {
				Files.createDirectories(tmp.getParent());
				InputStream is = getPdfFromUrl(url);
				Files.copy(is, tmp);
			}
			return Files.newInputStream(tmp);
		} catch (IOException e) {
			logger.error("failed to cache pdf: "+e.getMessage());
			return getPdfFromUrl(url);
		}
	}


	
	public InputStream getPdfFromUrl(String url, String filename) {
		return getPdfFromUrl(url, p -> p.resolve(filename));
	}
	
	public InputStream getPdfFromUrl(String url) {
		ClientConfig config = new DefaultClientConfig();
	    config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
	    Client client = Client.create(config);
	    if (debug) {
	    	client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
	    		@Override public void info(String msg) { logger.info(msg); }
	    	}));
	    }
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
	    
	    WebResource wr = client.resource(url);
	    
	    wr.addFilter(new ClientFilter() {
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
		return wr.get(InputStream.class);
	}
}
