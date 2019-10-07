package uk.co.terminological.bibliography.europepmc;

import java.util.Optional;

import uk.co.terminological.bibliography.ExtensibleJson;

/*
id: "7569999",
source: "MED",
citationType: "JOURNAL ARTICLE",
title: "Quantitative monitoring of gene expression patterns with a complementary DNA microarray.",
authorString: "Schena M, Shalon D, Davis RW, Brown PO.",
journalAbbreviation: "Science",
issue: "5235",
pubYear: 1995,
volume: "270",
pageInfo: "467-470",
citedOrder: 1,
match: "Y",
essn: "1095-9203",
issn: "0036-8075"
 */

public class Reference extends ExtensibleJson {
	
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
	
	public Optional<String> getCitationIndex() {return this.asString("citedOrder");}
	
}
