package uk.co.terminological.pubmedclient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;

public class EntrezResult {

	//TODO: Better (i.e. some) error handling

	public static class Search {

		private XmlElement raw;

		public Search(XmlElement raw) {
			this.raw = raw;
		}

		public XmlElement raw() {return raw;}

		public Optional<Integer> count() {
			try {
				return raw.doXpath("/eSearchResult/Count").getOne(XmlElement.class).getTextContent().map(s -> Integer.parseInt(s));
			} catch (XmlException e) {
				return Optional.empty();
			}
		}

		public Stream<String> getIds() {
			try {
				return raw.doXpath(".//Id")
						.getManyAsStream(XmlElement.class).flatMap(o -> o.getTextContent().stream());
			} catch (XmlException e) {
				return Stream.empty();
			}
		}

		public Optional<String> getWebEnv() {
			try {
				return raw.doXpath(".//WebEnv").getOne(XmlElement.class).getTextContent();
			} catch (XmlException e) {
				return Optional.empty();
			}
		}
		
		public Optional<String> getQueryKey() {
			try {
				return raw.doXpath(".//QueryKey").getOne(XmlElement.class).getTextContent();
			} catch (XmlException e) {
				return Optional.empty();
			}
		}
		
		public Optional<PubMedEntries> getStoredResult(EntrezClient client) throws BibliographicApiException {
			Optional<String> webEnv = getWebEnv(); 
			Optional<String> queryKey = getQueryKey();
			if (webEnv.isPresent() && queryKey.isPresent()) {
				return Optional.of(client.getPMEntriesByWebEnvAndQueryKey(webEnv.get(),queryKey.get()));
			}
			return Optional.empty();
		}

		
		
	}

	public static class PubMedEntries {

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

	public static class PubMedEntry {

		private XmlElement raw; //PubmedArticle

		public PubMedEntry(XmlElement raw) {this.raw = raw;}

		public XmlElement getRaw() {return raw;}

		public String getTitle() {
			try {
				return raw.doXpath(".//ArticleTitle").getOne(XmlElement.class).getTextContent().get();
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

		public Optional<String> getPMID() {
			try {
				return raw.doXpath(".//ArticleId[@IdType='pubmed']").get(XmlElement.class).flatMap(o -> o.getTextContent());
			} catch (XmlException e) {
				return Optional.empty();
			}
		}

		public Optional<LocalDate> getPubMedDate() {
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

		public String getAbstract() {return getAbstract(true);}
		public String getUnlabelledAbstract() {return getAbstract(false);}

		private String getAbstract(final boolean labelled) {
			try {
				return raw.doXpath(".//AbstractText").getManyAsStream(XmlElement.class)
						.map(el -> { if (labelled) { 
							return el.getAttributeValue("label").map(o -> o+"\t").orElse("")+el.getTextContent().orElse("");
						} else {
							return el.getTextContent().orElse("");
						}}).collect(Collectors.joining("\n"));
			} catch (XmlException e) {
				throw new RuntimeException(e);
			}
		}

		public Stream<Author> getAuthors() {
			try {
				return raw.doXpath(".//Author").getManyAsStream(XmlElement.class).map(o -> new Author(o));
			} catch (XmlException e) {
				return Stream.empty();
			}
		}
	}

	public static class Author {

		private XmlElement raw;
		public Author(XmlElement raw) {this.raw = raw;}
		public Optional<String> lastName() {
			return raw.childElements("LastName").findFirst().flatMap(o -> o.getTextContent());
		}
		public Optional<String> firstName() {
			return raw.childElements("FirstName").findFirst().flatMap(o -> o.getTextContent());
		}
		public Optional<String> initials() {
			return raw.childElements("Initials").findFirst().flatMap(o -> o.getTextContent());
		}
		public Stream<String> affiliations() {
			return raw.childElements("AffiliationInfo").stream()
					.flatMap(el -> el.childElements("Affiliation").stream())
					.flatMap(o -> o.getTextContent().stream());
		}
		public String getIdentifier() {
			return (lastName().orElse(UUID.randomUUID().toString())+"_"+initials().orElse("unknown").substring(0, 1)).toLowerCase();
		}
		public String toString() {
			return lastName().orElse("unknown")+", "+initials().orElse("unknown");
		}

	}

	public static class MeshHeading {
		private XmlElement raw;
		public MeshHeading(XmlElement raw) {this.raw = raw;}
		public MeshCode getDescriptor() {
			return raw.childElements("DescriptorName").stream().findFirst().map(o -> new MeshCode(o)).get();
		}
		public Stream<MeshCode> getQualifiers() {
			return raw.childElements("QualifierName").stream().map(o -> new MeshCode(o));
		}
		public String toString() { 
			return getDescriptor().toString()+ " ["+getQualifiers().map(q -> q.toString()).collect(Collectors.joining("; "))+"]";
		}
	}

	public static class MeshCode {
		private XmlElement raw;
		public MeshCode(XmlElement raw) {this.raw = raw;}
		public String getCode() { return raw.getAttributeValue("UI").get(); }
		public String getTerm() { return raw.getTextContent().get(); }
		public String toString() {return getCode()+":"+getTerm();}
	}


	public static class Links {

		private XmlElement raw;
		public Links(XmlElement raw) throws XmlException {
			this.raw=raw; 
			convert();
		}
		public XmlElement raw() {return raw;}

		private List<Link> links;

		public Stream<Link> stream() {
			return links.stream();
		}

		private void convert() throws XmlException {
			links = new ArrayList<Link>();
			
				for (XmlElement linkSet: raw.doXpath(".//LinkSet").getMany(XmlElement.class)) {

					Optional<String> dbFrom = linkSet.doXpath("./DbFrom").get(XmlElement.class).flatMap(el -> el.getTextContent());
					Optional<String> fromId = linkSet.doXpath("./IdList/Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
					
					for (XmlElement linkSetDb: linkSet.doXpath("./LinkSetDb").getMany(XmlElement.class)) {
						
						Optional<String> dbTo = linkSetDb.doXpath("./DbTo").get(XmlElement.class).flatMap(el -> el.getTextContent());
						Optional<String> linkName = linkSetDb.doXpath("./LinkName").get(XmlElement.class).flatMap(el -> el.getTextContent());
						
						for (XmlElement link: linkSetDb.doXpath("./Link").getMany(XmlElement.class)) {
							
							Optional<String> toId = link.doXpath("./Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
							Optional<Long> score = link.doXpath("./Score").get(XmlElement.class).flatMap(el -> el.getTextContent()).map(s -> Long.parseLong(s));
							
							if (dbFrom.isPresent() && fromId.isPresent() && toId.isPresent()) {
								links.add(new Link(dbFrom.get(),fromId.get(),linkName,dbTo,toId.get(), score));
							}
							
						}
						
					}
					
					for (XmlElement idUrlSet: linkSet.doXpath("./IdUrlList/IdUrlSet").getMany(XmlElement.class)) { 
						
						fromId = idUrlSet.doXpath("./Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
						for (XmlElement objUrl: idUrlSet.doXpath("./ObjUrl").getMany(XmlElement.class)) {
							
							Optional<String> category = objUrl.doXpath("./Category").get(XmlElement.class).flatMap(el -> el.getTextContent());
							Optional<String> toUrl = objUrl.doXpath("./Url").get(XmlElement.class).flatMap(el -> el.getTextContent());
							
							if (dbFrom.isPresent() && fromId.isPresent() && toUrl.isPresent()) {
								links.add(new Link(dbFrom.get(),fromId.get(),category,toUrl.get()));
							}
						}
					}

					
				}
			
		}
		public List<Link> getLinks() {
			return links;
		}



		//public List<String> getSimilar

	}

	/**
	 * 
	 * type: https://eutils.ncbi.nlm.nih.gov/entrez/query/static/entrezlinks.html
	 * 
	 * pmc_pmc_cites	Articles that Cites other	Articles that Cites other	PMC articles that given PMC article cites	3000000
	 * pmc_pmc_citedby	Cited Articles	Cited Articles	PMC article citing given PMC article	3000000
	 * pmc_pubmed	PubMed	PubMed Links	PubMed citations for these articles	10000
	 * pubmed_pmc	-	PMC Links	Free full-text versions of the current articles in the PubMed Central database.	10000
	 * pubmed_pmc_local	Free in PMC	-	Free full text articles in PMC	10000
	 * pubmed_pmc_refs	Cited in PMC	Cited in PMC	Full-text articles in the PubMed Central Database that cite the current articles.	10000
	 * pubmed_pubmed	Similar articles	Similar articles 	Calculated set of PubMed citations similar to the selected article(s) retrieved using a word weight algorithm. Similar articles are displayed in ranked order from most to least similar, with the linked from citation displayed first. 500
	 * pubmed_pubmed_refs	References for this PMC Article	References for PMC Articles	Citation referenced in PubMed article. Only valid for PubMed citations that are also in PMC.
	 * 
	 * @author terminological
	 *
	 */
	public static class Link {
		public String fromDb;
		public String fromId;
		public Optional<String> typeOrCategory = Optional.empty();
		public Optional<Long> score = Optional.empty();
		public String toDbOrUrl;
		public Optional<String> toId = Optional.empty();

		protected Link(String dbFrom, String fromId, Optional<String> category, String toUrl) {
			this.fromDb = dbFrom;
			this.fromId = fromId;
			this.typeOrCategory = category;
			this.toDbOrUrl = toUrl;
		}

		protected Link(String fromDb, String fromId, Optional<String> type, Optional<String> toDb, String toId, Optional<Long> score) {
			this.fromDb = fromDb;
			this.fromId = fromId;
			this.typeOrCategory = type;
			this.toDbOrUrl = toDb.orElse(fromDb);
			this.toId = Optional.of(toId);
			this.score = score;
		}

		public String toString() {
			return fromDb+"\t"+fromId+"\t"+typeOrCategory.orElse("")+"\t"+toDbOrUrl+"\t"+toId.orElse("")+"\t"+score.orElse(0L);
		}

	}
}
