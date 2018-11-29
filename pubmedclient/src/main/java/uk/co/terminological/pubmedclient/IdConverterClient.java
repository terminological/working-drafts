package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.isomorphism.util.TokenBuckets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class IdConverterClient extends CachingApiClient {

	private static final Logger logger = LoggerFactory.getLogger(IdConverterClient.class);
	
	private String developerEmail;
	private Client client;
	private String toolName;
	private static String URL = "https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/";
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	
	public static IdConverterClient create(String developerEmail, String toolName) {
		return new IdConverterClient(developerEmail,toolName, Optional.empty());
	}
	
	public static IdConverterClient create(String developerEmail, String toolName, Path cacheDir) {
		return new IdConverterClient(developerEmail,toolName, Optional.ofNullable(cacheDir));
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
	
	// Batches the calls to groups of max 50 ids
	//TODO: Refactor this to look up cache on a id by id basis.
	private Set<Record> getMapping(Collection<String> id2, IdType idType) throws BibliographicApiException {
		Set<Record> out = new HashSet<>();
		List<String> id = new ArrayList<String>(id2);
		int start = 0;
		while (start<id.size()) {
			int end = id.size()<start+50 ? id.size() : start+50;
			List<String> tmp2 = id.subList(start, end);
			Optional<Result> outTmp = doCall(tmp2,idType);
			outTmp.ifPresent(o -> out.addAll(o.records));
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
			.post();
	}
	
	
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
	
	public static enum IdType {
		PMCID,
		PMID,
		DOI,
		MID
	}
	
	
	//https://github.com/FasterXML/jackson-modules-java8
	public static class Result extends ExtensibleJson {
		@JsonProperty("status") public Optional<String> status = Optional.empty();
		@JsonProperty("responseDate") public Optional<String> responseDate = Optional.empty();
		@JsonProperty("request") public Optional<String> request = Optional.empty();
		@JsonProperty("records") public List<Record> records = Collections.emptyList();
	}
	
	public static class Record extends ExtensibleJson {
		@JsonProperty("pmcid") public Optional<String> pmcid = Optional.empty();
		@JsonProperty("pmid") public Optional<String> pmid = Optional.empty();
		@JsonProperty("doi") public Optional<String> doi = Optional.empty();
		@JsonProperty("live") public Optional<Boolean> live = Optional.empty();
		@JsonProperty("status") public Optional<String> status = Optional.empty();
		@JsonProperty("errmsg") public Optional<String> errmsg = Optional.empty();
		@JsonProperty("versions") public List<Version> versions = Collections.emptyList();
		@JsonProperty("release-date") public Optional<String> releaseDate = Optional.empty();
		
		public boolean idNotFound() { return status.orElse("ok").equals("error"); } 
	}
	
	public static class Version extends ExtensibleJson {
		@JsonProperty("pmcid") public Optional<String> pmcid = Optional.empty();
		@JsonProperty("mid") public Optional<String> pmid = Optional.empty();
		@JsonProperty("current") public Optional<Boolean> current = Optional.empty();
		@JsonProperty("live") public Optional<Boolean> live = Optional.empty();
		@JsonProperty("release-date") public Optional<String> releaseDate = Optional.empty();
	}

	
}
