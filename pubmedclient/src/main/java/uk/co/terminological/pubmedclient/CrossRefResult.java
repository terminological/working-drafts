package uk.co.terminological.pubmedclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/* 
Crossref Metadata API JSON Format
Versioning
Version	Release Date	Comments
v1	11th July 2016	First documented version
v2	26th July 2017	Add abstract, authenticated-orcid, fix contributor fields
v3	15th May 2018	Add peer review fields
 */

public class CrossRefResult {
	
	public static class ListResult extends ExtensibleJson {
		@JsonProperty("status") public String status;
		@JsonProperty("message-type") public String messageType;
		@JsonProperty("message-version") public String messageVersion;
		@JsonProperty("message") public Message message;
	}
	
	public static class SingleResult extends ExtensibleJson {
		@JsonProperty("status") public String status;
		@JsonProperty("message-type") public String messageType;
		@JsonProperty("message-version") public String messageVersion;
		@JsonProperty("message") public Work work;
	}
	
	//When message-type is work-list
	public static class Message extends ExtensibleJson {
		@JsonProperty("facets") public Facets facets;
		@JsonProperty("total-results") public Integer totalResults;
		@JsonProperty("items") public List<Work> items;
		@JsonProperty("items-per-page") public Integer itemsPerPage;
		@JsonProperty("query") public Query query;
		@JsonProperty("next-cursor") public String nextCursor;
	}
	
	public static class Facets extends ExtensibleJson {}
	
	public static class Query extends ExtensibleJson {
		@JsonProperty("start-index") public Integer startIndex;
		@JsonProperty("search-terms") public String searchTerm;
	}
	
	public static class Work {
		@JsonProperty("publisher") public String publisher; // Yes-Name of work's publisher
		@JsonProperty("title") public List<String> title = new ArrayList<>(); // Yes-Work titles, including translated titles
		@JsonProperty("original-title") public List<String> originalTitle = new ArrayList<>(); // No-Work titles in the work's original publication language
		@JsonProperty("short-title") public List<String> shortTitle = new ArrayList<>(); // No-Short or abbreviated work titles
		@JsonProperty("abstract") public String journalAbstract; // No-Abstract as a JSON string or a JATS XML snippet encoded into a JSON string
		@JsonProperty("reference-count") public Integer referenceCount; // Yes-Deprecated Same as references-count
		@JsonProperty("references-count") public Integer referencesCount; // Yes-Count of outbound references deposited with Crossref
		@JsonProperty("is-referenced-by-count") public Integer isReferencedByCount; // Yes-Count of inbound references deposited with Crossref
		@JsonProperty("source") public String source; // Yes-Currently always Crossref
		@JsonProperty("prefix") public String prefix; // Yes-DOI prefix identifier of the form http://id.crossref.org/prefix/DOIPREFIX
		@JsonProperty("DOI") public String DOI; // Yes-DOI of the work
		@JsonProperty("URL") public URL URL; // Yes-URL form of the work's DOI
		@JsonProperty("member") public String member; // Yes-Member identifier of the form http://id.crossref.org/member/MEMBERID
		@JsonProperty("type") public String type; // Yes-Enumeration, one of the type ids from https://api.crossref.org/v1/types
		@JsonProperty("created") public Date created; // Yes-Date on which the DOI was first registered
		@JsonProperty("deposited") public Date deposited; // Yes-Date on which the work metadata was most recently updated
		@JsonProperty("indexed") public Date indexed; // Yes-Date on which the work metadata was most recently indexed. Re-indexing does not imply a metadata change, see deposited for the most recent metadata change date
		@JsonProperty("issued") public PartialDate issued; // Yes-Earliest of published-print and published-online
		@JsonProperty("posted") public PartialDate posted; // No-Date on which posted content was made available online
		@JsonProperty("accepted") public PartialDate accepted; // No-Date on which a work was accepted, after being submitted, during a submission process
		@JsonProperty("subtitle") public List<String> subtitle = new ArrayList<>(); // No-Work subtitles, including original language and translated
		@JsonProperty("container-title") public List<String> containerTitle = new ArrayList<>(); // No-Full titles of the containing work (usually a book or journal)
		@JsonProperty("short-container-title") public List<String> shortContainerTitle = new ArrayList<>(); // No-Abbreviated titles of the containing work
		@JsonProperty("group-title") public String groupTitle; // No-Group title for posted content
		@JsonProperty("issue") public String issue; // No-Issue Integer of an article's journal
		@JsonProperty("volume") public String volume; // No-Volume Integer of an article's journal
		@JsonProperty("page") public String page; // No-Pages numbers of an article within its journal
		@JsonProperty("article-Integer") public String articleNumber; // No-
		@JsonProperty("published-print") public PartialDate publishedPrint; // No-Date on which the work was published in print
		@JsonProperty("published-online") public PartialDate publishedOnline; // No-Date on which the work was published online
		@JsonProperty("subject") public List<String> subject = new ArrayList<>(); // No-Subject category names, a controlled vocabulary from Sci-Val. Available for most journal articles
		@JsonProperty("ISSN") public List<String> ISSN = new ArrayList<>(); // No-
		@JsonProperty("issn-type") public List<ISSNWithType> issnType = new ArrayList<>(); // No-List of ISSNs with ISSN type information
		@JsonProperty("ISBN") public List<String> ISBN = new ArrayList<>(); // No-
		@JsonProperty("archive") public List<String> archive = new ArrayList<>(); // No-
		@JsonProperty("license") public List<License> license = new ArrayList<>(); // No-
		@JsonProperty("funder") public List<Funder> funder = new ArrayList<>(); // No-
		@JsonProperty("assertion") public List<Assertion> assertion = new ArrayList<>(); // No-
		@JsonProperty("author") public List<Contributor> author = new ArrayList<>(); // No-
		@JsonProperty("editor") public List<Contributor> editor = new ArrayList<>(); // No-
		@JsonProperty("chair") public List<Contributor> chair = new ArrayList<>(); // No-
		@JsonProperty("translator") public List<Contributor> translator = new ArrayList<>(); // No-
		@JsonProperty("update-to") public List<Update> updateTo = new ArrayList<>(); // No-
		@JsonProperty("update-policy") public URL updatePolicy; // No-Link to an update policy covering Crossmark updates for this work
		@JsonProperty("link") public List<ResourceLink> link = new ArrayList<>(); // No-URLs to full-text locations
		@JsonProperty("clinical-trial-Integer") public List<ClinicalTrialNumber> clinicalTrialNumber = new ArrayList<>(); // No-
		@JsonProperty("alternative-id") public List<String> alternativeId = new ArrayList<>(); // No-Other identifiers for the work provided by the depositing member
		@JsonProperty("reference") public List<Reference> reference = new ArrayList<>(); // No-List of references made by the work
		@JsonProperty("content-domain") public ContentDomain contentDomain; // No-Information on domains that support Crossmark for this work
		@JsonProperty("relation") public Map<String,List<Relation>> relation; // No-Relations to other works
		@JsonProperty("review") public Review review; // No-Peer review metadata
		@JsonProperty("language") public String language; //NOT IN SPEC
		@JsonProperty("score") public Float score; //NOT IN SPEC
		@JsonProperty("edition-number") public Integer editionNumber; //NOT IN SPEC
		@JsonProperty("event") public Event event; //NOT IN SPEC
		@JsonProperty("journal-issue") public JournalIssue journalIssue; //NOT IN SPEC
		@JsonProperty("isbn-type") public List<ISSNWithType> isbnType = new ArrayList<>(); //NOT IN SPEC
		@JsonProperty("publisher-location") public String publisherLocation; //NOT IN SPEC
		
	}

	public static class Event extends ExtensibleJson {
		@JsonProperty("name") public String name; //NOT IN SPEC
		@JsonProperty("location") public String location; //NOT IN SPEC
		@JsonProperty("theme") public String theme; //NOT IN SPEC
		@JsonProperty("end") public PartialDate end; //NOT IN SPEC
		@JsonProperty("start") public PartialDate start; //NOT IN SPEC
	}
	
	public static class JournalIssue extends ExtensibleJson {
		@JsonProperty("published-online") public PartialDate publishedOnline;
		@JsonProperty("published-print") public PartialDate publishedPrint;
		@JsonProperty("issue") public String issue;
	}
	
	public static class Funder extends ExtensibleJson {
		@JsonProperty("name") public String name; // Yes-Funding body primary name
		@JsonProperty("DOI") public String DOI; // No-Optional Open Funder Registry DOI uniquely identifing the funding body
		@JsonProperty("award") public List<String> award = new ArrayList<>(); // No-Award Integer(s) for awards given by the funding body
		@JsonProperty("doi-asserted-by") public String doiAssertedBy; // No-Either crossref or publisher
	}

	public static class ClinicalTrialNumber extends ExtensibleJson {
		@JsonProperty("clinical-trial-Integer") public String clinicalTrialNumber; // Yes-Identifier of the clinical trial
		@JsonProperty("registry") public String registry; // Yes-DOI of the clinical trial regsitry that assigned the trial Integer
		@JsonProperty("type") public String type; // No-One of preResults, results or postResults
	}

	public static class Contributor extends ExtensibleJson {
		@JsonProperty("family") public String family; // Yes-
		@JsonProperty("given") public String given; // No-
		@JsonProperty("sequence") public String sequence; // No-
		@JsonProperty("ORCID") public URL ORCID; // No-URL-form of an ORCID identifier
		@JsonProperty("authenticated-orcid") public Boolean authenticatedOrcid; // No-If true, record owner asserts that the ORCID user completed ORCID OAuth authentication
		@JsonProperty("affiliation") public List<Affiliation> affiliation = new ArrayList<>(); // No-
	}

	public static class Affiliation extends ExtensibleJson {
		@JsonProperty("name") public String name; // Yes-
	}

	public static class Date extends ExtensibleJson {
		@JsonProperty("date-parts") public List<List<Integer>> dateParts = new ArrayList<>(); // Yes-Contains an ordered array of year, month, day of month. Note that the field contains a nested array, e.g. [ [ 2006, 5, 19 ] ] to conform to citeproc JSON dates
		@JsonProperty("timestamp") public Long timestamp; // Yes-Seconds since UNIX epoch
		@JsonProperty("date-time") public String dateTime; // Yes-ISO 8601 date time
	}

	public static class PartialDate extends ExtensibleJson {
		@JsonProperty("date-parts") public List<List<Integer>> dateParts = new ArrayList<>(); // Yes-Contains an ordered array of year, month, day of month. Only year is required. Note that the field contains a nested array, e.g. [ [ 2006, 5, 19 ] ] to conform to citeproc JSON dates
	}

	public static class Update extends ExtensibleJson {
		@JsonProperty("updated") public PartialDate updated; // Yes-Date on which the update was published
		@JsonProperty("DOI") public String DOI; // Yes-DOI of the updated work
		@JsonProperty("type") public String type; // Yes-The type of update, for example retraction or correction
		@JsonProperty("label") public String label; // No-A display-friendly label for the update type
	}

	public static class Assertion extends ExtensibleJson {
		@JsonProperty("name") public String name; // Yes-
		@JsonProperty("value") public String value; // Yes-
		@JsonProperty("URL") public URL URL; // No-
		@JsonProperty("explanation") public URL explanation; // No-
		@JsonProperty("label") public String label; // No-
		@JsonProperty("order") public Integer order; // No-
		@JsonProperty("group") public AssertionGroup group; // No-
	}

	public static class AssertionGroup extends ExtensibleJson {
		@JsonProperty("name") public String name; // Yes-
		@JsonProperty("label") public String label; // No-
	}

	public static class License extends ExtensibleJson {
		@JsonProperty("content-version") public String contentVersion; // Yes-Either vor (version of record,) am (accepted manuscript,) tdm (text and data mining) or unspecified
		@JsonProperty("delay-in-days") public Integer delayInDays; // Yes-Integer of days between the publication date of the work and the start date of this license
		@JsonProperty("start") public Date start; // Yes-Date on which this license begins to take effect
		@JsonProperty("URL") public URL URL; // Yes-Link to a web page describing this license
	}

	public static class ResourceLink extends ExtensibleJson {
		@JsonProperty("intended-application") public String intendedApplication; // Yes-Either text-mining, similarity-checking or unspecified
		@JsonProperty("content-version") public String contentVersion; // Yes-Either vor (version of record,) am (accepted manuscript) or unspecified
		@JsonProperty("URL") public URL URL; // Yes-Direct link to a full-text download location
		@JsonProperty("content-type") public String contentType; // No-Content type (or MIME type) of the full-text object
	}

	public static class Reference extends ExtensibleJson {
		@JsonProperty("key") public String key; // Yes-
		@JsonProperty("DOI") public String DOI; // No-
		@JsonProperty("doi-asserted-by") public String doiAssertedBy; // No-One of crossref or publisher
		@JsonProperty("issue") public String issue; // No-
		@JsonProperty("first-page") public String firstPage; // No-
		@JsonProperty("volume") public String volume; // No-
		@JsonProperty("edition") public String edition; // No-
		@JsonProperty("component") public String component; // No-
		@JsonProperty("standard-designator") public String standardDesignator; // No-
		@JsonProperty("standards-body") public String standardsBody; // No-
		@JsonProperty("author") public String author; // No-
		@JsonProperty("year") public String year; // No-
		@JsonProperty("unstructured") public String unstructured; // No-
		@JsonProperty("journal-title") public String journalTitle; // No-
		@JsonProperty("article-title") public String articleTitle; // No-
		@JsonProperty("series-title") public String seriesTitle; // No-
		@JsonProperty("volume-title") public String volumeTitle; // No-
		@JsonProperty("ISSN") public String ISSN; // No-
		@JsonProperty("issn-type") public String issnType; // No-One of pissn or eissn
		@JsonProperty("ISBN") public String ISBN; // No-
		@JsonProperty("isbn-type") public String isbnType; // No-
	}

	public static class ISSNWithType extends ExtensibleJson {
		@JsonProperty("value") public String value; // Yes-
		@JsonProperty("type") public String type; // Yes-One of eissn, pissn or lissn
	}

	public static class ContentDomain extends ExtensibleJson {
		@JsonProperty("domain") public List<String> domain = new ArrayList<>(); // Yes-
		@JsonProperty("crossmark-restriction") public Boolean crossmarkRestriction; // Yes-
	}

	public static class Relation extends ExtensibleJson {
		@JsonProperty("id-type") public String idType; // Yes-
		@JsonProperty("id") public String id; // Yes-
		@JsonProperty("asserted-by") public String assertedBy; // Yes-One of subject or object
	}

	public static class Review extends ExtensibleJson {
		@JsonProperty("running-number") public String runningNumber; // No-
		@JsonProperty("revision-round") public String revisionRound; // No-
		@JsonProperty("stage") public String stage; // No-One of pre-publication or post-publication
		@JsonProperty("recommendation") public String recommendation; // No-One of major-revision or minor-revision or reject or reject-with-resubmit or accept
		@JsonProperty("type") public String type; // No-One of referee-report or editor-report or author-comment or community-comment or aggregate
		@JsonProperty("competing-interest-statement") public String competingInterestStatement; // No-
		@JsonProperty("language") public String language;	// String	No
	}


}
