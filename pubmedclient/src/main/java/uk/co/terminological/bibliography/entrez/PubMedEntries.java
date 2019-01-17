package uk.co.terminological.bibliography.entrez;

import java.util.stream.Stream;

import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;

public class PubMedEntries {

	private XmlElement raw; //PubmedArticleSet

	public PubMedEntries(XmlElement raw) {this.raw = raw;}

	public Stream<PubMedEntry> stream() {
		if (raw == null) return Stream.empty();
		return raw.childElements("PubmedArticle").stream().map(o-> new PubMedEntry(o));

	}

	public Stream<String> getTitles() {
		if (raw == null) return Stream.empty();
		try {
			return this.raw.doXpath(".//ArticleTitle")
					.getManyAsStream(XmlElement.class).flatMap(o -> o.getTextContent().stream());
		} catch (XmlException e) {
			return Stream.empty();
		}
	}

	public static PubMedEntries empty() {
		return new PubMedEntries(null);
	}

}