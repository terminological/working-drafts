package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

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

public class IdConverterClient {

	private static final Logger logger = LoggerFactory.getLogger(IdConverterClient.class);
	
	private String developerEmail;
	private Client client;
	private String toolName;
	private WebResource lookupService;
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
	
	public IdConverterClient(String developerEmail, String toolName) {
		this.developerEmail = developerEmail;
		this.toolName = toolName;
		this.client = Client.create();
		this.lookupService = client.resource("https://www.ncbi.nlm.nih.gov/pmc/utils/idconv/v1.0/");
	}
	
	public IdConverterClient debugMode() {
		this.client.addFilter(new LoggingFilter(new java.util.logging.Logger("Jersey",null) {
			@Override public void info(String msg) { logger.info(msg); }
		}));
		return this;
	}
	
	private MultivaluedMap<String, String> defaultApiParams() {
		MultivaluedMap<String, String> out = new MultivaluedMapImpl();
		out.add("tool", toolName);
		out.add("email", developerEmail);
		out.add("format", "json");
		return out;
	}
	
	public Result getMapping(String id) throws BibliographicApiException {
		return getMapping(Collections.singletonList(id), Optional.empty());
	}
	
	public Result getConverterForPMIds(Collection<String> id) throws BibliographicApiException {
		return getMapping(id, Optional.of(IdType.PMID));
	}
	
	public Result getConverterForIdsAndType(Collection<String> id, IdType type) throws BibliographicApiException {
		return getMapping(id, Optional.of(type));
	}
	
	// Batches the calls to groups of max 50 ids
	private Result getMapping(Collection<String> id2, Optional<IdType> idType) throws BibliographicApiException {
		List<String> id = new ArrayList<String>(id2);
		Result out = null;
		int start = 0;
		while (start<id.size()) {
			int end = id.size()<start+50 ? id.size() : start+50;
			List<String> tmp2 = id.subList(start, end);
			Result outTmp = doCall(tmp2,idType);
			if (out == null) out = outTmp; 
			else {
				out.records.addAll(outTmp.records);
			}
			start += 50;
		}
		return out;
	}
	
	//TODO: Change to list of records
	private Result doCall(Collection<String> id, Optional<IdType> idType) throws BibliographicApiException {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("ids", id.stream().collect(Collectors.joining(",")));
		id.forEach(i -> params.add("ids", i));
		if (idType.isPresent()) params.add("idtype", idType.get().name().toLowerCase());
		logger.debug("calling id converter with params: "+params);
		//WebResource wr = lookupService.queryParams(params);
		
		try {
			InputStream is = lookupService.post(InputStream.class,params); 
			Result  response = objectMapper.readValue(is, Result.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			e.printStackTrace();
			throw new BibliographicApiException("Malformed response", e);
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect", e);
		}
	}
	
	
	public Set<String> getDoisByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		Result tmp = getConverterForIdsAndType(ids, type);
		return tmp.records.stream()
				.flatMap(r -> r.doi.stream()).filter(o -> !o.isEmpty())
				.collect(Collectors.toSet());
	}
	
	public Set<String> getPubMedCentralIdsByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		return getConverterForIdsAndType(ids, type).records.stream()
				.flatMap(r -> r.pmcid.stream()).filter(o -> !o.isEmpty())
				.collect(Collectors.toSet());
	}
	
	public Set<String> getPMIdsByIdAndType(Collection<String> ids, IdType type) throws BibliographicApiException {
		return getConverterForIdsAndType(ids, type).records.stream()
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

	public static IdConverterClient create(String appId, String developerEmail) {
		return new IdConverterClient(appId,developerEmail);
	}
}
