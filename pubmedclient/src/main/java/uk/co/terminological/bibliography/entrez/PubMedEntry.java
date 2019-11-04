package uk.co.terminological.bibliography.entrez;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.PrintRecord;
import uk.co.terminological.bibliography.record.RecordReference;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlList;

public class PubMedEntry implements PrintRecord {

	private XmlElement raw; //PubmedArticle

	public PubMedEntry(XmlElement raw) {this.raw = raw;}

	public XmlElement getRaw() {return raw;}

	public Optional<String> getTitle() {
		try {
			return raw.doXpath(".//ArticleTitle").getOne(XmlElement.class).getTextContent();
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public Optional<String> getDoi() {
		try {
			return raw.doXpath(".//ArticleId[@IdType='doi']").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	public Optional<String> getPMCID() {
		try {
			return raw.doXpath(".//ArticleId[@IdType='pmc']").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * This will perform require a redirect
	 * @return
	 */
	public Optional<URI> getPdfUri() {
		return getPMCID().map(s -> "https://www.ncbi.nlm.nih.gov/pmc/articles/"+s+"/pdf").map(URI::create);
	}

	public Optional<String> getPMID() {
		try {
			return raw.doXpath(".//ArticleId[@IdType='pubmed']").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}
	
	public List<String> getKeyWords() {
		try {
			XmlList<XmlElement> keywords = raw.doXpath(".//KeywordList/Keyword").getMany(XmlElement.class);
			return keywords.stream().flatMap(xmlt -> xmlt.getTextContent().stream()).collect(Collectors.toList());
		} catch (XmlException e) {
			return Collections.emptyList();
		}
	}
	
	public List<String> getCitedPMIDs() {
		try {
			XmlList<XmlElement> keywords = raw.doXpath(".//CommentsCorrections[@RefType='Cites']/PMID").getMany(XmlElement.class);
			return keywords.stream().flatMap(xmlt -> xmlt.getTextContent().stream()).collect(Collectors.toList());
		} catch (XmlException e) {
			return Collections.emptyList();
		}
	}

	public Optional<LocalDate> getDate() {
		try {
			Optional<XmlElement> date = raw.doXpath(".//PubMedPubDate[@PubStatus='pubmed']").get(XmlElement.class);
			return date.flatMap(el -> {
				Optional<Integer> year = el.childElements("Year").findFirst().flatMap(el2 -> el2.getTextContent()).map(Integer::parseInt);
				Optional<Integer> month = el.childElements("Month").findFirst().flatMap(el2 -> el2.getTextContent()).map(Integer::parseInt);
				Optional<Integer> day = el.childElements("Day").findFirst().flatMap(el2 -> el2.getTextContent()).map(Integer::parseInt);
				return year.map(y -> LocalDate.of(y, month.orElse(1), day.orElse(1)));
			});
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	public Stream<MeshHeading> getMeshHeadings() {			
		try {
			return raw.doXpath(".//MeshHeading").getManyAsStream(XmlElement.class).map(o -> new MeshHeading(o));
		} catch (XmlException e) {
			return Stream.empty();
		}
	}

	public Optional<String> getAbstract() {return getAbstract(true);}
	public Optional<String> getUnlabelledAbstract() {return getAbstract(false);}

	private Optional<String> getAbstract(final boolean labelled) {
		try {
			String tmp = raw.doXpath(".//AbstractText").getManyAsStream(XmlElement.class)
					.map(el -> { if (labelled) { 
						return el.getAttributeValue("label").map(o -> o+"\t").orElse("")+el.getTextContent().orElse("");
					} else {
						return el.getTextContent().orElse("");
					}}).collect(Collectors.joining("\n"));
			return tmp.isEmpty() ? Optional.empty(): Optional.of(tmp);
		} catch (XmlException e) {
			throw new RuntimeException(e);
		}
	}

	public List<EntrezAuthor> getAuthors() {
		try {
			return raw.doXpath(".//Author").getManyAsStream(XmlElement.class).map(o -> new EntrezAuthor(o)).collect(Collectors.toList());
		} catch (XmlException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public Optional<String> getIdentifier() {
		return getPMID();
	}

	@Override
	public IdType getIdentifierType() {
		return IdType.PMID;
	}

	@Override
	public Set<RecordReference> getOtherIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<String> getLicenses() {
		return Stream.empty();
	}

	@Override
	public Optional<String> getJournal() {
		try {
			return raw.doXpath(".//Article/Journal/ISOAbbreviation").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getFirstAuthorName() {
		return this.getFirstAuthor().map(o -> o.getLastName());
	}

	@Override
	public Optional<String> getVolume() {
		try {
			return raw.doXpath(".//Article/Journal/JournalIssue/Volume").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getIssue() {
		try {
			return raw.doXpath(".//Article/Journal/JournalIssue/Issue").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Long> getYear() {
		try {
			return raw.doXpath(".//Article/Journal/JournalIssue/PubDate/Year").get(XmlElement.class).flatMap(o -> o.getTextContent()).map(Long::parseLong);
		} catch (XmlException e) {
			return Optional.empty();
		}
	
	}

	@Override
	public Optional<String> getPage() {
		try {
			return raw.doXpath(".//Article/Pagination/MedlinePgn").get(XmlElement.class).flatMap(o -> o.getTextContent());
		} catch (XmlException e) {
			return Optional.empty();
		}
	}

	
}