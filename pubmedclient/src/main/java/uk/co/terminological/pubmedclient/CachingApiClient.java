package uk.co.terminological.pubmedclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

import uk.co.terminological.pubmedclient.CrossRefClient.BinaryData;

public class CachingApiClient {

	protected CacheManager cacheManager;

	protected static class BinaryData implements Serializable {
			byte[] byteArray;
			private BinaryData(InputStream is) throws IOException {
				byteArray = IOUtils.toByteArray(is);
			}
			public static BinaryData from(InputStream is) throws BibliographicApiException {
				try {
					return new BinaryData(is);
				} catch (IOException e) {
					throw new BibliographicApiException("Could not read api response",e);
				}
			}
			public InputStream get() {
				return new ByteArrayInputStream(byteArray);
			}
		}

	protected Cache<String,BinaryData> foreverCache() {
		return cacheManager.getCache("forever", String.class, BinaryData.class);
	}

	protected Cache<String,BinaryData> weekCache() {
		return cacheManager.getCache("week", String.class, BinaryData.class);
	}

	public CrossRefClient debugMode() {
		this.client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
			@Override public void info(String msg) { logger.info(msg); }
		}));
		return this;
	}

	protected static final Logger logger = LoggerFactory.getLogger(CrossRefClient.class);
	protected Client client;
	protected TokenBucket rateLimiter = TokenBuckets.builder().withCapacity(50).withInitialTokens(50).withFixedIntervalRefillStrategy(50, 1, TimeUnit.SECONDS).build();

	protected static String encode(String string) {
		try {
			return URLEncoder.encode(string,java.nio.charset.StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected static String keyFromApiQuery(String url, MultivaluedMap<String, String> params) {
		return 
				"{\"url\":\""+url+"\",\"params\":{"+
				params.keySet().stream().sorted()
				.map(t ->  
				"\""+t+"\":["+params.get(t)
				.stream().sorted().map(s -> "\""+s.replace("\"", "\\\"")+"\"")
				.collect(Collectors.joining(","))+"]")
				.collect(Collectors.joining(","))+"}}";
	}

	protected void updateRateLimits(MultivaluedMap<String, String> headers) {
		try {
	
			Long rateLimitRequests = Long.parseLong(headers.get("X-Rate-Limit-Limit").get(0));
			Long rateLimitInterval = Long.parseLong(headers.get("X-Rate-Limit-Interval").get(0).replace("s", ""));
			rateLimiter = TokenBuckets.builder().withInitialTokens(rateLimiter.getNumTokens()).withCapacity(rateLimitRequests).withFixedIntervalRefillStrategy(rateLimitRequests,rateLimitInterval,TimeUnit.SECONDS).build();
		} catch (Exception e) {
			//Probably header wasn't set - just ignore
		}
	}

	public CachingApiClient() {
		super();
	}

}