package uk.co.terminological.bibliography.europepmc;

import java.util.Optional;

import uk.co.terminological.bibliography.ExtensibleJson;


/*

id: "20981092",
source: "MED",
title: "A map of human genome variation from population-scale sequencing.",
authorString: "1000 Genomes Project Consortium, Abecasis GR, Altshuler D, Auton A, Brooks LD, Durbin RM, Gibbs RA, Hurles ME, McVean GA.",
journalTitle: "Nature",
issue: "7319",
journalVolume: "467",
pubYear: "2010",
journalIssn: "0028-0836; 1476-4687; ",
pageInfo: "1061-1073",

pmid: "20981092",
pmcid: "PMC3042601",
doi: "10.1038/nature09534",




pubType: "research support, n.i.h., intramural; research support, non-u.s. gov't; research-article; research support, u.s. gov't, non-p.h.s.; journal article; research support, n.i.h., extramural",
isOpenAccess: "N",
inEPMC: "Y",
inPMC: "Y",
hasPDF: "Y",
hasBook: "N",
hasSuppl: "Y",
citedByCount: 4184,
hasReferences: "Y",
hasTextMinedTerms: "Y",
hasDbCrossReferences: "N",
hasLabsLinks: "Y",
hasTMAccessionNumbers: "N",
firstIndexDate: "2010-12-03",
firstPublicationDate: "2010-10-01"
 */

public class LiteResult extends ExtensibleJson {

	public Optional<String> getID() {return this.asString("id");}
	public Optional<String> getSource() {return this.asString("source");}
	public Optional<String> getTitle() {return this.asString("title");}
	public Optional<String> getAuthors() {return this.asString("authorString");}
	public Optional<String> getJournal() {return this.asString("journalTitle");}
	public Optional<String> getIssue() {return this.asString("issue");}
	public Optional<String> getVolume() {return this.asString("journalVolume");}
	public Optional<Long> getYear() {return this.asString("pubYear").map(Long::parseLong);}
	public Optional<String> getPages() {return this.asString("pageInfo");}
	// public Optional<String> getEssn() {return this.asString("essn");}
	public Optional<String> getIssn() {return this.asString("journalIssn");}
	
	public Optional<Long> getCitedByCount() {return this.asLong("citedByCount");
	
	
	}
}
