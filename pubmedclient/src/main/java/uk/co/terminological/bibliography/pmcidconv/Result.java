package uk.co.terminological.bibliography.pmcidconv;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

//https://github.com/FasterXML/jackson-modules-java8
public class Result implements Iterable<Record> {
	@JsonProperty("status") public Optional<String> status = Optional.empty();
	@JsonProperty("responseDate") public Optional<String> responseDate = Optional.empty();
	@JsonProperty("request") public Optional<String> request = Optional.empty();
	@JsonProperty("records") public List<Record> records = Collections.emptyList();
	@Override
	
	public Iterator<Record> iterator() {
		return records.iterator();
	}
}