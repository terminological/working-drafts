package uk.co.terminological.pubmedclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

import uk.co.terminological.datatypes.StreamExceptions;



public class CachingApiClient {

	protected CacheManager cacheManager;
	
	protected CachingApiClient(Optional<Path> optional, TokenBucket ratelimiter) {
		this.client = Client.create();
		if (optional.isPresent()) {
			StreamExceptions.tryRethrow(t -> Files.createDirectories(optional.get()));
			this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
					.with(CacheManagerBuilder.persistence(optional.get().toFile())) 
					.withCache(
							"forever",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BinaryData.class, 
									ResourcePoolsBuilder
									.heap(1000)
									.disk(40, MemoryUnit.MB, true))
							.withExpiry(ExpiryPolicyBuilder.noExpiration()))
					.withCache(
							"week",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BinaryData.class, 
									ResourcePoolsBuilder
									.heap(1000)
									.disk(40, MemoryUnit.MB, true))
							.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(7)))) 
					.build(); 
		} else {
			this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
					.withCache(
							"forever",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BinaryData.class, 
									ResourcePoolsBuilder
									.heap(10000)
									)
							.withExpiry(ExpiryPolicyBuilder.noExpiration())) 
					.withCache(
							"week",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, BinaryData.class, 
									ResourcePoolsBuilder
									.heap(10000)
									).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(7)))
							)
					.build();
		}
		cacheManager.init();
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				cacheManager.close();
			}
		});
		this.rateLimiter = ratelimiter;
	}

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

	public void debugMode() {
		this.client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
			@Override public void info(String msg) { logger.info(msg); }
		}));
	}

	private static final Logger logger = LoggerFactory.getLogger(CachingApiClient.class);
	protected Client client;
	protected TokenBucket rateLimiter;
	
	public void rateLimit() {rateLimiter.consume();}
	
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