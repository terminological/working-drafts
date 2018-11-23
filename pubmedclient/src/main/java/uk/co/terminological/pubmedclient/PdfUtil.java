package uk.co.terminological.pubmedclient;

import java.io.InputStream;

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

public class PdfUtil {

	static Logger logger = LoggerFactory.getLogger(PdfUtil.class);
	
	public static InputStream getPdfFromUrl(String url) {
		return getPdfFromUrl(url, 10, false);
	}
	
	public static InputStream getPdfFromUrl(String url, int maxRedirects, boolean debug) {
		ClientConfig config = new DefaultClientConfig();
	    config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
	    Client client = Client.create(config);
	    if (debug) {
	    	client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
	    		@Override public void info(String msg) { logger.info(msg); }
	    	}));
	    }
	    client.setFollowRedirects(true);
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
