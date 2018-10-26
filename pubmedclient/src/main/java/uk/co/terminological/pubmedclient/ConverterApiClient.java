package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import uk.co.terminological.pubmedclient.CrossRefClient.CrossRefException;

public class ConverterApiClient {

	
	private String developerEmail;
	private Client client;
	private String toolName;
	private WebResource lookupService;
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public ConverterApiClient(String developerEmail, String toolName) {
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
	
	public Response getMapping(String id) throws CrossRefException {
		return getMapping(Collections.singletonList(id), Optional.empty());
	}
	
	public Response getMapping(List<String> id) throws CrossRefException {
		return getMapping(id, Optional.empty());
	}
	
	public Response getMapping(List<String> id, IdType type) throws CrossRefException {
		return getMapping(id, Optional.of(type));
	}
	
	public Response getMapping(List<String> id, Optional<IdType> idType) throws CrossRefException {
		Response out = null;
		int start = 0;
		while (start<id.size()) {
			int end = id.size()<start+200 ? id.size() : start+200;
			List<String> tmp2 = id.subList(start, end);
			Response outTmp = doCall(tmp2,idType);
			if (out == null) out = outTmp; 
			else {
				out.records.addAll(outTmp.records);
			}
			start += 200;
		}
		return out;
	}
	
	private Response doCall(List<String> id, Optional<IdType> idType) throws CrossRefException {
		MultivaluedMap<String, String> params = defaultApiParams();
		params.add("ids", id.stream().collect(Collectors.joining(",")));
		if (idType.isPresent()) params.add("idtype", idType.get().name().toLowerCase());
		WebResource wr = lookupService.queryParams(params);
		try {
			InputStream is = wr.get(InputStream.class); 
			Response  response = objectMapper.readValue(is, Response.class);
			return response;
		} catch (JsonParseException | JsonMappingException e) {
			e.printStackTrace();
			throw new CrossRefException("Malformed response");
		} catch (IOException | UniformInterfaceException e) {
			throw new CrossRefException("Cannot connect");
		}
	}
	
	
	public List<String> getDoisFor(List<String> ids, IdType type) throws CrossRefException {
		return getMapping(ids, type).records.stream()
				.map(r -> r.doi).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public List<String> getPubmedCentralIdsFor(List<String> ids, IdType type) throws CrossRefException {
		return getMapping(ids, type).records.stream()
				.map(r -> r.pmcid).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public List<String> getPubmedIdsFor(List<String> ids, IdType type) throws CrossRefException {
		return getMapping(ids, type).records.stream()
				.map(r -> r.pmid).filter(o -> o != null)
				.collect(Collectors.toList());
	}
	
	public static enum IdType {
		PMCID,
		PMID,
		DOI,
		MID
	}
	
	public static class Response extends ExtensibleJson {
		@JsonProperty("status") public String status;
		@JsonProperty("responseDate") public String responseDate;
		@JsonProperty("request") public String request;
		@JsonProperty("records") public List<Record> records;
	}
	
	public static class Record extends ExtensibleJson {
		@JsonProperty("pmcid") public String pmcid;
		@JsonProperty("pmid") public String pmid;
		@JsonProperty("doi") public String doi;
		@JsonProperty("versions") public List<Version> versions;
	}
	
	public static class Version extends ExtensibleJson {
		@JsonProperty("pmcid") public String pmcid;
		@JsonProperty("mid") public String pmid;
		@JsonProperty("current") public Boolean current;
		@JsonProperty("live") public Boolean live;
		@JsonProperty("releaseDate") public String releaseDate;
	}

	public static ConverterApiClient create(String appId, String developerEmail) {
		return new ConverterApiClient(appId,developerEmail);
	}
}
