package uk.co.terminological.pubmedclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.co.terminological.pubmedclient.CrossRefApiResponse.Message;

public class ConverterApiClient {

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
}
