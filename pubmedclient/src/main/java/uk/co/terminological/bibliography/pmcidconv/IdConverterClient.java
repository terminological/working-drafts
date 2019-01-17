package uk.co.terminological.bibliography.pmcidconv;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.ehcache.Cache;
import org.ehcache.spi.loaderwriter.CacheLoadingException;
import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.bibliography.BibliographicApiException;
import uk.co.terminological.bibliography.BinaryData;
import uk.co.terminological.bibliography.CachingApiClient;
import uk.co.terminological.bibliography.record.IdType;

public class IdConverterClient extends CachingApiClient {

	private static final Logger logger = LoggerFactory.getLogger(IdConverterClient.class);
	private static Map<String, IdConverterClient> instances = new HashMap<>();
	
	private String developerEmail;
	private String toolName;
	private static String URL = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	
	public static IdConverterClient create(String developerEmail, String toolName) {
		return new IdConverterClient(developerEmail,toolName, Optional.empty());
	}
	
	public static IdConverterClient create(String developerEmail, String toolName, Path cacheDir) {
		if (!instances.containsKey(developerEmail)) {
			instances.put(developerEmail, new IdConverterClient(developerEmail,toolName, Optional.ofNullable(cacheDir)));
		}
		return instances.get(developerEmail);	
	}
	
	private IdConverterClient(String developerEmail, String toolName, Optional<Path> cacheDir) {
		super ( cacheDir, TokenBuckets.builder().withInitialTokens(1000).withCapacity(1000).withFixedIntervalRefillStrategy(1000, 24*6*6, TimeUnit.SECONDS).build());
		this.developerEmail = developerEmail;
		this.toolName = toolName;
	}
	
	protected MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("tool", toolName);
		out.add("email", developerEmail);
		out.add("format", "json");
		return out;
	}
	
	private String keyFrom(String id, IdType idType) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("ids", id);
		params.add("idtype", idType.name().toLowerCase());
		return keyFromApiQuery(URL,params);
	}
	
	// Batches the calls to groups of max 50 ids
	public Set<Record> getMapping(Collection<String> id2, IdType idType) throws BibliographicApiException {
		Set<Record> out = new HashSet<>();
		Cache<String,BinaryData> cache = permanentCache();
		List<String> id = new ArrayList<String>(id2);
		
		for (String nextId: id2) {
			if (cache.containsKey(keyFrom(nextId,idType))) {
				try {
					Record tmp = objectMapper.readValue(
							cache.get(keyFrom(nextId,idType)).inputStream(), 
							Record.class);
					out.add(tmp);
				} catch (CacheLoadingException | IOException e) {
					logger.debug("error parsing cached content for: {}, {}",nextId,idType);
					cache.remove(keyFrom(nextId,idType));
					id.add(nextId);
				} 
			} else {
				id.add(nextId);
			}
		}
		int start = 0;
		while (start<id.size()) {
			int end = id.size()<start+50 ? id.size() : start+50;
			List<String> tmp2 = id.subList(start, end);
			Optional<Result> outTmp = doCall(tmp2,idType);
			outTmp.ifPresent(o -> {
				out.addAll(o.records);
				o.records.forEach(r -> {
					//Add component to cache at individual level
					BinaryData ser;
					try {
						ser = BinaryData.from(objectMapper.writeValueAsBytes(r));
						r.pmid.ifPresent(i -> cache.put(keyFrom(i,IdType.PMID), ser));
						r.doi.ifPresent(i -> cache.put(keyFrom(i,IdType.DOI), ser));
						r.pmcid.ifPresent(i -> cache.put(keyFrom(i,IdType.PMCID), ser));
					} catch (JsonProcessingException e) {
						logger.warn("Could not serialise id converter record to json");
					}
				});
			});
			start += 50;
		}
		return out;
	}
	
	private Optional<Result> doCall(Collection<String> id, IdType idType) {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("ids", id.stream().collect(Collectors.joining(",")));
		params.add("idtype", idType.name().toLowerCase());
		logger.debug("calling id converter with params: "+params);
		return this.buildCall(URL, Result.class)
			.cacheForever()
			.withParams(params)
			.withOperation(is -> objectMapper.readValue(is, Result.class))
			.get();
	}
	
	//TODO: Map output to this....
	public Set<String> getDoisByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		Set<Record> tmp = getMapping(ids, type);
		return tmp.stream()
				.flatMap(r -> r.doi.stream()).filter(o -> !o.isEmpty())
				.collect(Collectors.toSet());
	}
	
	public Set<String> getPubMedCentralIdsByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		return getMapping(ids, type).stream()
				.flatMap(r -> r.pmcid.stream()).filter(o -> !o.isEmpty())
				.collect(Collectors.toSet());
	}
	
	public Set<String> getPMIdsByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		return getMapping(ids, type).stream()
				.flatMap(r -> r.pmid.stream()).filter(o -> !o.isEmpty())
				.collect(Collectors.toSet());
	}

	
}
