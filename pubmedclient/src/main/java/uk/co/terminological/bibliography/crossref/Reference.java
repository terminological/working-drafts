package uk.co.terminological.bibliography.crossref;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.PrintRecord;
import uk.co.terminological.bibliography.record.RecordReference;

public class Reference extends ExtensibleJson implements RecordReference, PrintRecord {
	
	public Reference(JsonNode node) { super(node); }
	
	public Optional<String> getIdentifier() {return this.asString("DOI");}
	public Optional<String> getTitle() {return this.asString("article-title");}
	public Optional<String> getFirstAuthorName() {return this.asString("author");}
	public Optional<String> getJournal() {return this.asString("journal-title");}
	public Optional<String> getVolume() {return this.asString("volume");}
	public Optional<String> getIssue() {return this.asString("issue");}
	public Optional<Long> getYear() {return this.asLong("year");}
	public Optional<String> getPage() {return this.asString("first-page");}

	@Override
	public IdType getIdentifierType() {
		return this.asString("DOI").isPresent() ? IdType.DOI : IdType.MID;
	}
	
	
	/*@JsonProperty("key") public Optional<String> key = Optional.empty(); // Yes-
	@JsonProperty("DOI") public Optional<String> DOI = Optional.empty(); // No-
	@JsonProperty("doi-asserted-by") public Optional<String> doiAssertedBy = Optional.empty(); // No-One of crossref or publisher
	@JsonProperty("issue") public Optional<String> issue = Optional.empty(); // No-
	@JsonProperty("first-page") public Optional<String> firstPage = Optional.empty(); // No-
	@JsonProperty("volume") public Optional<String> volume = Optional.empty(); // No-
	@JsonProperty("edition") public Optional<String> edition = Optional.empty(); // No-
	@JsonProperty("component") public Optional<String> component = Optional.empty(); // No-
	@JsonProperty("standard-designator") public Optional<String> standardDesignator = Optional.empty(); // No-
	@JsonProperty("standards-body") public Optional<String> standardsBody = Optional.empty(); // No-
	@JsonProperty("author") public Optional<String> author = Optional.empty(); // No-
	@JsonProperty("year") public Optional<String> year = Optional.empty(); // No-
	@JsonProperty("unstructured") public Optional<String> unstructured = Optional.empty(); // No-
	@JsonProperty("journal-title") public Optional<String> journalTitle = Optional.empty(); // No-
	@JsonProperty("article-title") public Optional<String> articleTitle = Optional.empty(); // No-
	@JsonProperty("series-title") public Optional<String> seriesTitle = Optional.empty(); // No-
	@JsonProperty("volume-title") public Optional<String> volumeTitle = Optional.empty(); // No-
	@JsonProperty("ISSN") public Optional<String> ISSN = Optional.empty(); // No-
	@JsonProperty("issn-type") public Optional<String> issnType = Optional.empty(); // No-One of pissn or eissn
	@JsonProperty("ISBN") public Optional<String> ISBN = Optional.empty(); // No-
	@JsonProperty("isbn-type") public Optional<String> isbnType = Optional.empty(); // No-*/
}