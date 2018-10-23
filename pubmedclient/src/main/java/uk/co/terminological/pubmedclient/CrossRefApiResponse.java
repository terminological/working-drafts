package uk.co.terminological.pubmedclient;

import java.util.List;
import java.util.HashMap;

public class CrossRefApiResponse {




	public static class Work {

		@JsonProperty("publisher") public String _publisher_; // Yes-Name of work's publisher
		@JsonProperty("title") @JsonProperty("_title_") public List<String> __title__; // Yes-Work titles, including translated titles
		@JsonProperty("original-title") public List<String> _original-title_; // No-Work titles in the work's original publication language
		@JsonProperty("short-title") public List<String> _short-title_; // No-Short or abbreviated work titles
		public XML String abstract; // No-Abstract as a JSON string or a JATS XML snippet encoded into a JSON string
		@JsonProperty("reference-count") public Number _reference-count_; // Yes-Deprecated Same as references-count
		@JsonProperty("references-count") public Number _references-count_; // Yes-Count of outbound references deposited with Crossref
		@JsonProperty("is-referenced-by-count") public Number _is-referenced-by-count_; // Yes-Count of inbound references deposited with Crossref
		@JsonProperty("source") public String _source_; // Yes-Currently always Crossref
		@JsonProperty("prefix") public String _prefix_; // Yes-DOI prefix identifier of the form http://id.crossref.org/prefix/DOI_PREFIX
		@JsonProperty("DOI") public String _DOI_; // Yes-DOI of the work
		@JsonProperty("URL") public URL _URL_; // Yes-URL form of the work's DOI
		@JsonProperty("member") public String _member_; // Yes-Member identifier of the form http://id.crossref.org/member/MEMBER_ID
		@JsonProperty("type") public String _type_; // Yes-Enumeration, one of the type ids from https://api.crossref.org/v1/types
		@JsonProperty("created") public Date _created_; // Yes-Date on which the DOI was first registered
		@JsonProperty("deposited") public Date _deposited_; // Yes-Date on which the work metadata was most recently updated
		@JsonProperty("indexed") public Date _indexed_; // Yes-Date on which the work metadata was most recently indexed. Re-indexing does not imply a metadata change, see deposited for the most recent metadata change date
		@JsonProperty("issued") public PartialDate _issued_; // Yes-Earliest of published-print and published-online
		@JsonProperty("posted") public PartialDate _posted_; // No-Date on which posted content was made available online
		@JsonProperty("accepted") public PartialDate _accepted_; // No-Date on which a work was accepted, after being submitted, during a submission process
		@JsonProperty("subtitle") public List<String> _subtitle_; // No-Work subtitles, including original language and translated
		@JsonProperty("container-title") public List<String> _container-title_; // No-Full titles of the containing work (usually a book or journal)
		@JsonProperty("short-container-title") public List<String> _short-container-title_; // No-Abbreviated titles of the containing work
		@JsonProperty("group-title") public String _group-title_; // No-Group title for posted content
		@JsonProperty("issue") public String _issue_; // No-Issue number of an article's journal
		@JsonProperty("volume") public String _volume_; // No-Volume number of an article's journal
		@JsonProperty("page") public String _page_; // No-Pages numbers of an article within its journal
		@JsonProperty("article-number") public String _article-number_; // No-
		@JsonProperty("published-print") public PartialDate _published-print_; // No-Date on which the work was published in print
		@JsonProperty("published-online") public PartialDate _published-online_; // No-Date on which the work was published online
		@JsonProperty("subject") public List<String> _subject_; // No-Subject category names, a controlled vocabulary from Sci-Val. Available for most journal articles
		@JsonProperty("ISSN") public List<String> _ISSN_; // No-
		@JsonProperty("issn-type") public List<ISSNWithType> _issn-type_; // No-List of ISSNs with ISSN type information
		@JsonProperty("ISBN") public List<String> _ISBN_; // No-
		@JsonProperty("archive") public List<String> _archive_; // No-
		@JsonProperty("license") public List<License> _license_; // No-
		@JsonProperty("funder") public List<Funder> _funder_; // No-
		@JsonProperty("assertion") public List<Assertion> _assertion_; // No-
		@JsonProperty("author") public List<Contributor> _author_; // No-
		@JsonProperty("editor") public List<Contributor> _editor_; // No-
		@JsonProperty("chair") public List<Contributor> _chair_; // No-
		@JsonProperty("translator") public List<Contributor> _translator_; // No-
		@JsonProperty("update-to") public List<Update> _update-to_; // No-
		@JsonProperty("update-policy") public URL _update-policy_; // No-Link to an update policy covering Crossmark updates for this work
		public List<Resource> Link link; // No-URLs to full-text locations
		@JsonProperty("clinical-trial-number") public List<ClinicalTrialNumber> _clinical-trial-number_; // No-
		@JsonProperty("alternative-id") public String _alternative-id_; // No-Other identifiers for the work provided by the depositing member
		@JsonProperty("reference") public List<Reference> _reference_; // No-List of references made by the work
		@JsonProperty("content-domain") public ContentDomain _content-domain_; // No-Information on domains that support Crossmark for this work
		@JsonProperty("relation") public Relations _relation_; // No-Relations to other works
		@JsonProperty("review") public Review _review_; // No-Peer review metadata



	}

	public static class Funder {

		@JsonProperty("name") public String _name_; // Yes-Funding body primary name
		@JsonProperty("DOI") public String _DOI_; // No-Optional Open Funder Registry DOI uniquely identifing the funding body
		@JsonProperty("award") public List<String> _award_; // No-Award number(s) for awards given by the funding body
		@JsonProperty("doi-asserted-by") public String _doi-asserted-by_; // No-Either crossref or publisher


	}

	public static class ClinicalTrialNumber {

		@JsonProperty("clinical-trial-number") public String _clinical-trial-number_; // Yes-Identifier of the clinical trial
		@JsonProperty("registry") public String _registry_; // Yes-DOI of the clinical trial regsitry that assigned the trial number
		@JsonProperty("type") public String _type_; // No-One of preResults, results or postResults


	}

	public static class Contributor {

		@JsonProperty("family") public String _family_; // Yes-
		@JsonProperty("given") public String _given_; // No-
		@JsonProperty("ORCID") public URL _ORCID_; // No-URL-form of an ORCID identifier
		@JsonProperty("authenticated-orcid") public Boolean _authenticated-orcid_; // No-If true, record owner asserts that the ORCID user completed ORCID OAuth authentication
		@JsonProperty("affiliation") public List<Affiliation> _affiliation_; // No-


	}

	public static class Affiliation {

		@JsonProperty("name") public String _name_; // Yes-


	}

	public static class Date {

		@JsonProperty("date-parts") public List<Number> _date-parts_; // Yes-Contains an ordered array of year, month, day of month. Note that the field contains a nested array, e.g. [ [ 2006, 5, 19 ] ] to conform to citeproc JSON dates
		@JsonProperty("timestamp") public Number _timestamp_; // Yes-Seconds since UNIX epoch
		@JsonProperty("date-time") public String _date-time_; // Yes-ISO 8601 date time


	}

	public static class PartialDate {

		@JsonProperty("date-parts") public List<Number> _date-parts_; // Yes-Contains an ordered array of year, month, day of month. Only year is required. Note that the field contains a nested array, e.g. [ [ 2006, 5, 19 ] ] to conform to citeproc JSON dates

	}

	public static class Update {

		@JsonProperty("updated") public PartialDate _updated_; // Yes-Date on which the update was published
		@JsonProperty("DOI") public String _DOI_; // Yes-DOI of the updated work
		@JsonProperty("type") public String _type_; // Yes-The type of update, for example retraction or correction
		@JsonProperty("label") public String _label_; // No-A display-friendly label for the update type


	}

	public static class Assertion {

		@JsonProperty("name") public String _name_; // Yes-
		@JsonProperty("value") public String _value_; // Yes-
		@JsonProperty("URL") public URL _URL_; // No-
		@JsonProperty("explanation") public URL _explanation_; // No-
		@JsonProperty("label") public String _label_; // No-
		@JsonProperty("order") public Number _order_; // No-
		@JsonProperty("group") public AssertionGroup _group_; // No-


	}

	public static class AssertionGroup {

		@JsonProperty("name") public String _name_; // Yes-
		@JsonProperty("label") public String _label_; // No-


	}

	public static class License {

		@JsonProperty("content-version") public String _content-version_; // Yes-Either vor (version of record,) am (accepted manuscript,) tdm (text and data mining) or unspecified
		@JsonProperty("delay-in-days") public Number _delay-in-days_; // Yes-Number of days between the publication date of the work and the start date of this license
		@JsonProperty("start") public PartialDate _start_; // Yes-Date on which this license begins to take effect
		@JsonProperty("URL") public URL _URL_; // Yes-Link to a web page describing this license


	}

	public static class ResourceLink {

		@JsonProperty("intended-application") public String _intended-application_; // Yes-Either text-mining, similarity-checking or unspecified
		@JsonProperty("content-version") public String _content-version_; // Yes-Either vor (version of record,) am (accepted manuscript) or unspecified
		@JsonProperty("URL") public URL _URL_; // Yes-Direct link to a full-text download location
		@JsonProperty("content-type") public String _content-type_; // No-Content type (or MIME type) of the full-text object


	}

	public static class Reference {

		@JsonProperty("key") public String _key_; // Yes-
		@JsonProperty("DOI") public String _DOI_; // No-
		@JsonProperty("doi-asserted-by") public String _doi-asserted-by_; // No-One of crossref or publisher
		@JsonProperty("issue") public String _issue_; // No-
		@JsonProperty("first-page") public String _first-page_; // No-
		@JsonProperty("volume") public String _volume_; // No-
		@JsonProperty("edition") public String _edition_; // No-
		@JsonProperty("component") public String _component_; // No-
		@JsonProperty("standard-designator") public String _standard-designator_; // No-
		@JsonProperty("standards-body") public String _standards-body_; // No-
		@JsonProperty("author") public String _author_; // No-
		@JsonProperty("year") public String _year_; // No-
		@JsonProperty("unstructured") public String _unstructured_; // No-
		@JsonProperty("journal-title") public String _journal-title_; // No-
		@JsonProperty("article-title") public String _article-title_; // No-
		@JsonProperty("series-title") public String _series-title_; // No-
		@JsonProperty("volume-title") public String _volume-title_; // No-
		@JsonProperty("ISSN") public String _ISSN_; // No-
		@JsonProperty("issn-type") public String _issn-type_; // No-One of pissn or eissn
		@JsonProperty("ISBN") public String _ISBN_; // No-
		@JsonProperty("isbn-type") public String _isbn-type_; // No-
	}

	public static class ISSNWithType {

		@JsonProperty("value") public String _value_; // Yes-
		@JsonProperty("type") public String _type_; // Yes-One of eissn, pissn or lissn
	}

	public static class ContentDomain {

		@JsonProperty("domain") public List<String> _domain_; // Yes-
		@JsonProperty("crossmark-restriction") public Boolean _crossmark-restriction_; // Yes-


	}

	public static class Relations extends HashMap<String, Relation> {
		
	}

	public static class Relation {

		@JsonProperty("id-type") public String _id-type_; // Yes-
		@JsonProperty("id") public String _id_; // Yes-
		@JsonProperty("asserted-by") public String _asserted-by_; // Yes-One of subject or object


	}

	public static class Review {

		@JsonProperty("running-number") public String _running-number_; // No-
		@JsonProperty("revision-round") public String _revision-round_; // No-
		@JsonProperty("stage") public String _stage_; // No-One of pre-publication or post-publication
		@JsonProperty("recommendation") public String _recommendation_; // No-One of major-revision or minor-revision or reject or reject-with-resubmit or accept
		@JsonProperty("type") public String _type_; // No-One of referee-report or editor-report or author-comment or community-comment or aggregate
		@JsonProperty("competing-interest-statement") public String _competing-interest-statement_; // No-
		@JsonProperty("language") public String _language_;	// String	No
	}
	/*
	 * 
Crossref Metadata API JSON Format
Versioning
Version	Release Date	Comments
v1	11th July 2016	First documented version
v2	26th July 2017	Add abstract, authenticated-orcid, fix contributor fields
v3	15th May 2018	Add peer review fields
	 */


}
