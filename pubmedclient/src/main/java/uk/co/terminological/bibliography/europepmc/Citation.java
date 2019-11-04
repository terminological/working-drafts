package uk.co.terminological.bibliography.europepmc;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.record.RecordReference;
import uk.co.terminological.bibliography.record.Paper;

/*

id: "31481663",
source: "MED",
citationType: "research-article; journal article",
title: "Pituitary cell translation and secretory capacities are enhanced cell autonomously by the transcription factor Creb3l2.",
authorString: "Khetchoumian K, Balsalobre A, Mayran A, Christian H, Chénard V, St-Pierre J, Drouin J.",
journalAbbreviation: "Nat Commun",
pubYear: 2019,
volume: "10",
issue: "1",
pageInfo: "3960",
citedByCount: 0 

*/

//TODO: change API to make a first class citation object
//TODO: get this to conform

public class Citation extends ExtensibleJson implements Paper, RecordReference {

	public Citation(JsonNode node) { super(node); }
	
	public Optional<String> getID() {return this.asString("id");}
	public Optional<String> getSource() {return this.asString("source");}
	public Optional<String> getTitle() {return this.asString("title");}
	public Optional<String> getAuthors() {return this.asString("authorString");}
	public Optional<String> getJournal() {return this.asString("journalAbbreviation");}
	public Optional<String> getIssue() {return this.asString("issue");}
	public Optional<String> getVolume() {return this.asString("volume");}
	public Optional<Long> getYear() {return this.asLong("pubYear");}
	public Optional<String> getPages() {return this.asString("pageInfo");}
	public Optional<String> getEssn() {return this.asString("essn");}
	public Optional<String> getIssn() {return this.asString("issn");}
	
	public Optional<Long> getCitedByCount() {return this.asLong("citedByCount");}
	
}
