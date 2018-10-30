package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
	
	public Result getConverterForPMIds(List<String> id) throws BibliographicApiException {
		return getMapping(id, Optional.of(IdType.PMID));
	}
	
	public Result getConverterForIdsAndType(List<String> id, IdType type) throws BibliographicApiException {
		return getMapping(id, Optional.of(type));
	}
	
	private Result getMapping(List<String> id, Optional<IdType> idType) throws BibliographicApiException {
		Result out = null;
		int start = 0;
		while (start<id.size()) {
			int end = id.size()<start+200 ? id.size() : start+200;
			List<String> tmp2 = id.subList(start, end);
			Result outTmp = doCall(tmp2,idType);
			if (out == null) out = outTmp; 
			else {
				out.records.addAll(outTmp.records);
			}
			start += 200;
		}
		return out;
	}
	
	private Result doCall(List<String> id, Optional<IdType> idType) throws BibliographicApiException {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("ids", id.stream().collect(Collectors.joining(",")));
		id.forEach(i -> params.add("ids", i));
		if (idType.isPresent()) params.add("idtype", idType.get().name().toLowerCase());
		logger.debug("calling id converter with params: "+params);
		WebResource wr = lookupService.queryParams(params);
		try {
			InputStream is = wr.get(InputStream.class); 
			Result  response = objectMapper.readValue(is, Result.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			e.printStackTrace();
			throw new BibliographicApiException("Malformed response");
		} catch (IOException | UniformInterfaceException e) {
			throw new BibliographicApiException("Cannot connect");
		}
	}
	
	
	public List<String> getDoisByIdAndType(List<String> ids, IdType type) throws BibliographicApiException {
		Result tmp = getConverterForIdsAndType(ids, type);
		return tmp.records.stream()
				.flatMap(r -> r.doi.stream()).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public List<String> getPubMedCentralIdsByIdAndType(List<String> ids, IdType type) throws BibliographicApiException {
		return getConverterForIdsAndType(ids, type).records.stream()
				.flatMap(r -> r.pmcid.stream()).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public List<String> getPMIdsByIdAndType(List<String> ids, IdType type) throws BibliographicApiException {
		return getConverterForIdsAndType(ids, type).records.stream()
				.flatMap(r -> r.pmid.stream()).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public static enum IdType {
		PMCID,
		PMID,
		DOI,
		MID
	}
	
	
	//https://github.com/FasterXML/jackson-modules-java8
	//TODO: replace with optionals
	public static class Result extends ExtensibleJson {
		@JsonProperty("status") public Optional<String> status;
		@JsonProperty("responseDate") public Optional<String> responseDate;
		@JsonProperty("request") public Optional<String> request;
		@JsonProperty("records") public List<Record> records;
	}
	
	public static class Record extends ExtensibleJson {
		@JsonProperty("pmcid") public Optional<String> pmcid;
		@JsonProperty("pmid") public Optional<String> pmid;
		@JsonProperty("doi") public Optional<String> doi;
		@JsonProperty("versions") public List<Version> versions;
	}
	
	public static class Version extends ExtensibleJson {
		@JsonProperty("pmcid") public Optional<String> pmcid;
		@JsonProperty("mid") public Optional<String> pmid;
		@JsonProperty("current") public Optional<Boolean> current;
		@JsonProperty("live") public Optional<Boolean> live;
		@JsonProperty("releaseDate") public Optional<String> releaseDate;
	}

	public static IdConverterClient create(String appId, String developerEmail) {
		return new IdConverterClient(appId,developerEmail);
	}
}
