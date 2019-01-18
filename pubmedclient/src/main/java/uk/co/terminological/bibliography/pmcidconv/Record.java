package uk.co.terminological.bibliography.pmcidconv;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.co.terminological.bibliography.record.IdType;

public class Record {
	@JsonProperty("pmcid") public Optional<String> pmcid = Optional.empty();
	@JsonProperty("pmid") public Optional<String> pmid = Optional.empty();
	@JsonProperty("doi") public Optional<String> doi = Optional.empty();
	@JsonProperty("live") public Optional<Boolean> live = Optional.empty();
	@JsonProperty("status") public Optional<String> status = Optional.empty();
	@JsonProperty("errmsg") public Optional<String> errmsg = Optional.empty();
	@JsonProperty("versions") public List<Version> versions = Collections.emptyList();
	@JsonProperty("release-date") public Optional<String> releaseDate = Optional.empty();
	
	public boolean idNotFound() { return status.orElse("ok").equals("error"); }
	
	public Optional<String> idByType(IdType type) {
		if (type.equals(IdType.DOI)) return doi;
		if (type.equals(IdType.PMID)) return pmid;
		if (type.equals(IdType.PMCID)) return pmcid;
		return Optional.empty();
	}
}