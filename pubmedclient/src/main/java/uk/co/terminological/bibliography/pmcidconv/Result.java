package uk.co.terminological.bibliography.pmcidconv;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

//https://github.com/FasterXML/jackson-modules-java8
public class Result {
	@JsonProperty("status") public Optional<String> status = Optional.empty();
	@JsonProperty("responseDate") public Optional<String> responseDate = Optional.empty();
	@JsonProperty("request") public Optional<String> request = Optional.empty();
	@JsonProperty("records") public List<Record> records = Collections.emptyList();
}