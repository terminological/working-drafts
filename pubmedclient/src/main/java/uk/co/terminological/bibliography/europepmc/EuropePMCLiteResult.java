package uk.co.terminological.bibliography.europepmc;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.europepmc.EuropePMCClient.DataSources;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.Print;
import uk.co.terminological.bibliography.record.RecordReference;


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

citedByCount: 4184,
hasReferences: "Y",



pubType: "research support, n.i.h., intramural; research support, non-u.s. gov't; research-article; research support, u.s. gov't, non-p.h.s.; journal article; research support, n.i.h., extramural",
isOpenAccess: "N",
inEPMC: "Y",
inPMC: "Y",
hasPDF: "Y",
hasBook: "N",
hasSuppl: "Y",
hasTextMinedTerms: "Y",
hasDbCrossReferences: "N",
hasLabsLinks: "Y",
hasTMAccessionNumbers: "N",
firstIndexDate: "2010-12-03",
firstPublicationDate: "2010-10-01"
 */

public class EuropePMCLiteResult extends ExtensibleJson implements RecordReference, Print {

	public EuropePMCLiteResult(JsonNode node) { super(node); }
	
	public Optional<String> getIdentifier() {return this.asString("id");}
	public Optional<DataSources> getSource() {return this.asString("source").map(DataSources::valueOf);}
	public Optional<String> getTitle() {return this.asString("title");}
	public Optional<String> getAuthorString() {return this.asString("authorString");}
	public Optional<String> getJournal() {return this.asString("journalTitle");}
	public Optional<String> getIssue() {return this.asString("issue");}
	public Optional<String> getVolume() {return this.asString("journalVolume");}
	public Optional<Long> getYear() {return this.asString("pubYear").map(Long::parseLong);}
	public Optional<String> getPage() {return this.asString("pageInfo");}
	// public Optional<String> getEssn() {return this.asString("essn");}
	public Optional<String> getIssn() {return this.asString("journalIssn");}
	
	public Optional<Long> getCitedByCount() {return this.asLong("citedByCount");}
	
	public Optional<String> getPMID() {return this.asString("pmid");}
	public Optional<String> getPMCID() {return this.asString("pmcid");}
	public Optional<String> getDOI() {return this.asString("doi");}
	
	public Optional<Boolean> hasReferences() {return this.asString("hasReferences").map(r -> r.equals("Y"));}

	@Override
	public IdType getIdentifierType() {
		if (!getSource().isPresent()) return IdType.UNK;
		if (getSource().get().equals(DataSources.MED)) return IdType.PMID;
		if (getSource().get().equals(DataSources.PMC)) return IdType.PMCID;
		return IdType.UNK;
	}

	@Override
	public Optional<String> getFirstAuthorName() {
		return getAuthorString().map(s -> s.split(",")[0]);
	}

	

	
	
	
	
}
