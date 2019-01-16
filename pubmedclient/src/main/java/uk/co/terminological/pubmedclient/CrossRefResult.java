package uk.co.terminological.pubmedclient;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

// import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.pubmedclient.record.Author;
import uk.co.terminological.pubmedclient.record.Citation;
import uk.co.terminological.pubmedclient.record.IdType;
import uk.co.terminological.pubmedclient.record.PrintRecord;
import uk.co.terminological.pubmedclient.record.RecordReference;
import uk.co.terminological.pubmedclient.record.RecordWithCitations;

/* 
Crossref Metadata API JSON Format
Versioning
Version	Release Date	Comments
v1	11th July 2016	First documented version
v2	26th July 2017	Add abstract, authenticated-orcid, fix contributor fields
v3	15th May 2018	Add peer review fields
 */
// https://github.com/json-path/JsonPath

public class CrossRefResult {
	
	public static class ListResult extends ExtensibleJson {
		
		public ListResult(JsonNode node) { super(node); }
		
		public Optional<String> getStatus() {return this.asString("status");}
		public Message getMessage() {return this.asObject(Message.class, "message").get();}
		
	}
	
	public static class SingleResult extends ExtensibleJson {
		public SingleResult(JsonNode node) { super(node); }
		
		public Optional<String> getStatus() {return this.asString("status");}
		public Work getWork() {return this.asObject(Work.class, "message").get();}
		
	}
	
	//When message-type is work-list
	public static class Message extends ExtensibleJson {
		public Message(JsonNode node) { super(node); }
		
		public Optional<String> getNextCursor() {return this.asString("next-cursor");}
		public Stream<Work> getItems() {return this.streamNode(Work.class, "items");}
		public Optional<Long> getTotalResults() {return this.asLong("total-results");}
		
	}
	
	public static class Work extends ExtensibleJson implements PrintRecord, RecordWithCitations {
		public Work(JsonNode node) {super(node);}
		
		public String getIdentifier() {return this.asString("DOI").get();}
		public IdType getIdentifierType() {return IdType.DOI;}
 		public String getTitle() {return this.streamPath("title").findFirst().map(
 				n -> n.asString()).orElse(getJournal());}
 		public String getFirstAuthorName() {
 			return this.getAuthors().findFirst().map(o -> o.getLastName()).orElse("Unknown");
 		}
 		public String getJournal() {return this.asString("container-title").orElse("No title");}
 		public Optional<String> getVolume() {return this.asString("volume");}
 		public Optional<String> getIssue() {return this.asString("issue");}
 		public Optional<Long> getYear() {
 			return this.streamPath("published-print","date-parts")
 					.findFirst().stream() // weird nested array
 					.findFirst().map(n -> n.asLong());
 			}
 		public Optional<String> getPage() {return this.asString("page");}
		
 		public Optional<LocalDate> getDate() {
 			return this.streamPath("create","date-parts","date-time").map(n -> n.asString())
 				.map(s -> LocalDate.parse(s, DateTimeFormatter.ISO_DATE_TIME))
 				.findFirst();
 		}
 		
 		
 		
 		
 		
 		public Optional<Double> getScore() {return this.asDouble("score");}
		public Stream<String> getLicenses() {return this.streamPath("license","URL").map(o -> o.asString());}
		
 		public Stream<Contributor> getAuthors() {return this.streamPath(Contributor.class, "links");}
 		public Stream<Citation> getCitations() {return this.streamPath(Reference.class, "reference");}
		public Optional<Long> getCitedByCount() {return this.asLong("is-referenced-by-count");}
		public Long getReferencesCount() {return this.asLong("references-count").get();}
 		public Optional<String> getAbstract() {return this.asString("abstract");}
 		
 		public Optional<URI> getTextMiningUri() {
 			// text-mining, similarity-checking or unspecified
 			return this
 					.streamPath("links")
 					.filter(n -> n.asString("intended-application").filter(s -> s.equals("text-mining")).isPresent())
 					.flatMap(n -> n.asString("URL").stream())
 					.map(URI::create)
 					.findFirst();
 		}

		@Override
		public Set<RecordReference> getOtherIdentifiers() {
			return Collections.emptySet();
		}

		@Override
		public Optional<URI> getPdfUri() {
			return Optional.empty();
		}

		
 		
		/*@JsonProperty("publisher") public Optional<String> publisher = Optional.empty(); // Yes-Name of work's publisher
		@JsonProperty("title") public List<String> title = Collections.emptyList(); // Yes-Work titles, including translated titles
		@JsonProperty("original-title") public List<String> originalTitle = Collections.emptyList(); // No-Work titles in the work's original publication language
		@JsonProperty("short-title") public List<String> shortTitle = Collections.emptyList(); // No-Short or abbreviated work titles
		@JsonProperty("abstract") public Optional<String> journalAbstract = Optional.empty(); // No-Abstract as a JSON string or a JATS XML snippet encoded into a JSON string
		@JsonProperty("reference-count") public Optional<Integer> referenceCount = Optional.empty(); // Yes-Deprecated Same as references-count
		@JsonProperty("references-count") public Optional<Integer> referencesCount = Optional.empty(); // Yes-Count of outbound references deposited with Crossref
		@JsonProperty("is-referenced-by-count") public Optional<Integer> isReferencedByCount = Optional.empty(); // Yes-Count of inbound references deposited with Crossref
		@JsonProperty("source") public Optional<String> source = Optional.empty(); // Yes-Currently always Crossref
		@JsonProperty("prefix") public Optional<String> prefix = Optional.empty(); // Yes-DOI prefix identifier of the form http://id.crossref.org/prefix/DOIPREFIX
		@JsonProperty("DOI") public Optional<String> DOI = Optional.empty(); // Yes-DOI of the work
		@JsonProperty("URL") public Optional<URL> URL = Optional.empty(); // Yes-URL form of the work's DOI
		@JsonProperty("member") public Optional<String> member = Optional.empty(); // Yes-Member identifier of the form http://id.crossref.org/member/MEMBERID
		@JsonProperty("type") public Optional<String> type = Optional.empty(); // Yes-Enumeration, one of the type ids from https://api.crossref.org/v1/types
		@JsonProperty("created") public Optional<Date> created = Optional.empty(); // Yes-Date on which the DOI was first registered
		@JsonProperty("deposited") public Optional<Date> deposited = Optional.empty(); // Yes-Date on which the work metadata was most recently updated
		@JsonProperty("indexed") public Optional<Date> indexed = Optional.empty(); // Yes-Date on which the work metadata was most recently indexed. Re-indexing does not imply a metadata change, see deposited for the most recent metadata change date
		@JsonProperty("issued") public Optional<PartialDate> issued = Optional.empty(); // Yes-Earliest of published-print and published-online
		@JsonProperty("posted") public Optional<PartialDate> posted = Optional.empty(); // No-Date on which posted content was made available online
		@JsonProperty("accepted") public Optional<PartialDate> accepted = Optional.empty(); // No-Date on which a work was accepted, after being submitted, during a submission process
		@JsonProperty("subtitle") public List<String> subtitle = Collections.emptyList(); // No-Work subtitles, including original language and translated
		@JsonProperty("container-title") public List<String> containerTitle = Collections.emptyList(); // No-Full titles of the containing work (usually a book or journal)
		@JsonProperty("short-container-title") public List<String> shortContainerTitle = Collections.emptyList(); // No-Abbreviated titles of the containing work
		@JsonProperty("group-title") public Optional<String> groupTitle = Optional.empty(); // No-Group title for posted content
		@JsonProperty("issue") public Optional<String> issue = Optional.empty(); // No-Issue Integer of an article's journal
		@JsonProperty("volume") public Optional<String> volume = Optional.empty(); // No-Volume Integer of an article's journal
		@JsonProperty("page") public Optional<String> page = Optional.empty(); // No-Pages numbers of an article within its journal
		@JsonProperty("article-number") public Optional<String> articleNumber = Optional.empty(); // No-
		@JsonProperty("published-print") public Optional<PartialDate> publishedPrint = Optional.empty(); // No-Date on which the work was published in print
		@JsonProperty("published-online") public Optional<PartialDate> publishedOnline = Optional.empty(); // No-Date on which the work was published online
		@JsonProperty("subject") public List<String> subject = Collections.emptyList(); // No-Subject category names, a controlled vocabulary from Sci-Val. Available for most journal articles
		@JsonProperty("ISSN") public List<String> ISSN = Collections.emptyList(); // No-
		@JsonProperty("issn-type") public List<ISSNWithType> issnType = Collections.emptyList(); // No-List of ISSNs with ISSN type information
		@JsonProperty("ISBN") public List<String> ISBN = Collections.emptyList(); // No-
		@JsonProperty("archive") public List<String> archive = Collections.emptyList(); // No-
		@JsonProperty("license") public List<License> license = Collections.emptyList(); // No-
		@JsonProperty("funder") public List<Funder> funder = Collections.emptyList(); // No-
		@JsonProperty("assertion") public List<Assertion> assertion = Collections.emptyList(); // No-
		@JsonProperty("author") public List<Contributor> author = Collections.emptyList(); // No-
		@JsonProperty("editor") public List<Contributor> editor = Collections.emptyList(); // No-
		@JsonProperty("chair") public List<Contributor> chair = Collections.emptyList(); // No-
		@JsonProperty("translator") public List<Contributor> translator = Collections.emptyList(); // No-
		@JsonProperty("update-to") public List<Update> updateTo = Collections.emptyList(); // No-
		@JsonProperty("update-policy") public Optional<URL> updatePolicy = Optional.empty(); // No-Link to an update policy covering Crossmark updates for this work
		@JsonProperty("link") public List<ResourceLink> link = Collections.emptyList(); // No-URLs to full-text locations
		@JsonProperty("clinical-trial-Integer") public List<ClinicalTrialNumber> clinicalTrialNumber = Collections.emptyList(); // No-
		@JsonProperty("alternative-id") public List<String> alternativeId = Collections.emptyList(); // No-Other identifiers for the work provided by the depositing member
		@JsonProperty("reference") public List<Reference> reference = Collections.emptyList(); // No-List of references made by the work
		@JsonProperty("content-domain") public Optional<ContentDomain> contentDomain = Optional.empty(); // No-Information on domains that support Crossmark for this work
		@JsonProperty("relation") public Map<String,List<Relation>> relation; // No-Relations to other works
		@JsonProperty("review") public Optional<Review> review = Optional.empty(); // No-Peer review metadata
		@JsonProperty("language") public Optional<String> language = Optional.empty(); //NOT IN SPEC
		@JsonProperty("score") public Optional<Float> score = Optional.empty(); //NOT IN SPEC
		@JsonProperty("edition-number") public Optional<Integer> editionNumber = Optional.empty(); //NOT IN SPEC
		@JsonProperty("event") public Optional<Event> event = Optional.empty(); //NOT IN SPEC
		@JsonProperty("journal-issue") public Optional<JournalIssue> journalIssue = Optional.empty(); //NOT IN SPEC
		@JsonProperty("isbn-type") public List<ISSNWithType> isbnType = Collections.emptyList(); //NOT IN SPEC
		@JsonProperty("publisher-location") public Optional<String> publisherLocation = Optional.empty(); //NOT IN SPEC
		*/
	}

	/*public static class Event extends ExtensibleJson {
		@JsonProperty("name") public Optional<String> name = Optional.empty(); //NOT IN SPEC
		@JsonProperty("location") public Optional<String> location = Optional.empty(); //NOT IN SPEC
		@JsonProperty("theme") public Optional<String> theme = Optional.empty(); //NOT IN SPEC
		@JsonProperty("end") public Optional<PartialDate> end = Optional.empty(); //NOT IN SPEC
		@JsonProperty("start") public Optional<PartialDate> start = Optional.empty(); //NOT IN SPEC
	}
	
	public static class JournalIssue extends ExtensibleJson {
		@JsonProperty("published-online") public Optional<PartialDate> publishedOnline = Optional.empty();
		@JsonProperty("published-print") public Optional<PartialDate> publishedPrint = Optional.empty();
		@JsonProperty("issue") public Optional<String> issue = Optional.empty();
	}
	
	public static class Funder extends ExtensibleJson {
		@JsonProperty("name") public Optional<String> name = Optional.empty(); // Yes-Funding body primary name
		@JsonProperty("DOI") public Optional<String> DOI = Optional.empty(); // No-Optional Open Funder Registry DOI uniquely identifing the funding body
		@JsonProperty("award") public List<String> award = Collections.emptyList(); // No-Award Integer(s) for awards given by the funding body
		@JsonProperty("doi-asserted-by") public Optional<String> doiAssertedBy = Optional.empty(); // No-Either crossref or publisher
	}

	public static class ClinicalTrialNumber extends ExtensibleJson {
		@JsonProperty("clinical-trial-Integer") public Optional<String> clinicalTrialNumber = Optional.empty(); // Yes-Identifier of the clinical trial
		@JsonProperty("registry") public Optional<String> registry = Optional.empty(); // Yes-DOI of the clinical trial regsitry that assigned the trial Integer
		@JsonProperty("type") public Optional<String> type = Optional.empty(); // No-One of preResults, results or postResults
	}*/

	public static class Contributor extends ExtensibleJson implements Author {
		
		public Contributor(JsonNode node) { super(node); }
		
		public String getLastName() {return this.asString("family").orElse("Unknown");}
		public Optional<String> getORCID() {return this.asString("ORCID");}
		public Stream<String> getAffiliations() {return this.streamNode("affiliations").flatMap(n -> n.asString("name").stream());}
		public Optional<String> getFirstName() {return this.asString("given");}
		public boolean isFirst() {return this.asString("sequence").filter(s -> s.equals("first")).isPresent();}
		public String getLabel() {
			return (getFirstName().orElse("Unknown")+", "+getLastName().substring(0, 1)).toLowerCase();
		}
		@Override
		public Optional<String> getInitials() {
			return getFirstName().map(o -> o.substring(0, 1));
		}
	}



	public static class Reference extends ExtensibleJson implements RecordReference, PrintRecord {
		
		public Reference(JsonNode node) { super(node); }
		
		public String getIdentifier() {return this.asString("DOI").orElse(getLabel());}
		public Optional<String> getTitle() {return this.asString("article-title");}
 		public String getFirstAuthorName() {return this.asString("author").orElse("Unknown");}
 		public String getJournal() {return this.asString("journal-title").orElse("Unknown");}
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

	/*public static class ISSNWithType extends ExtensibleJson {
		@JsonProperty("value") public Optional<String> value = Optional.empty(); // Yes-
		@JsonProperty("type") public Optional<String> type = Optional.empty(); // Yes-One of eissn, pissn or lissn
	}*/

	/*
	public static class ContentDomain extends ExtensibleJson {
		@JsonProperty("domain") public List<String> domain = Collections.emptyList(); // Yes-
		@JsonProperty("crossmark-restriction") public Optional<Boolean> crossmarkRestriction = Optional.empty(); // Yes-
	}

	public static class Relation extends ExtensibleJson {
		@JsonProperty("id-type") public Optional<String> idType = Optional.empty(); // Yes-
		@JsonProperty("id") public Optional<String> id = Optional.empty(); // Yes-
		@JsonProperty("asserted-by") public Optional<String> assertedBy = Optional.empty(); // Yes-One of subject or object
	}

	public static class Review extends ExtensibleJson {
		@JsonProperty("running-number") public Optional<String> runningNumber = Optional.empty(); // No-
		@JsonProperty("revision-round") public Optional<String> revisionRound = Optional.empty(); // No-
		@JsonProperty("stage") public Optional<String> stage = Optional.empty(); // No-One of pre-publication or post-publication
		@JsonProperty("recommendation") public Optional<String> recommendation = Optional.empty(); // No-One of major-revision or minor-revision or reject or reject-with-resubmit or accept
		@JsonProperty("type") public Optional<String> type = Optional.empty(); // No-One of referee-report or editor-report or author-comment or community-comment or aggregate
		@JsonProperty("competing-interest-statement") public Optional<String> competingInterestStatement = Optional.empty(); // No-
		@JsonProperty("language") public Optional<String> language = Optional.empty();	// String	No
	}*/


}
