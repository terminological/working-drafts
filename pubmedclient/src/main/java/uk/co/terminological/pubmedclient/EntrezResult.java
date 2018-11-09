package uk.co.terminological.pubmedclient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.nih.nlm.ncbi.eutils.generated.elink.ELinkResult;
import gov.nih.nlm.ncbi.eutils.generated.elink.Id;
import gov.nih.nlm.ncbi.eutils.generated.elink.IdUrlList;
import gov.nih.nlm.ncbi.eutils.generated.elink.IdUrlSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.LinkSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.LinkSetDb;
import gov.nih.nlm.ncbi.eutils.generated.elink.ObjUrl;
import gov.nih.nlm.ncbi.eutils.generated.esearch.Count;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;

public class EntrezResult {

	public static class Search {

		private ESearchResult raw;

		public Search(ESearchResult raw) {
			this.raw = raw;
		}

		public ESearchResult raw() {return raw;}

		public Optional<Integer> count() {
			return raw
					.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR().stream()
					.filter(o -> o instanceof Count).map(o -> (Count) o)
					.findFirst()
					.map(c -> Integer.parseInt(c.getvalue()));			
		}
		
		public List<String> getIds() {
			return raw.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR()
				.stream()
				.filter(o -> (o instanceof gov.nih.nlm.ncbi.eutils.generated.esearch.IdList))
				.map(o -> (gov.nih.nlm.ncbi.eutils.generated.esearch.IdList) o)
				.flatMap(idl -> idl.getId().stream())
				.map(id -> id.getvalue())
				.collect(Collectors.toList());
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
				throw new RuntimeException(e);
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
				throw new RuntimeException(e);
			}
		}
		
		public Optional<String> getPMCID() {
			try {
				return raw.doXpath(".//ArticleId[@IdType='pmc']").get(XmlElement.class).flatMap(o -> o.getTextContent());
			} catch (XmlException e) {
				throw new RuntimeException(e);
			}
		}
		
		public Optional<String> getPMID() {
			try {
				return raw.doXpath(".//ArticleId[@IdType='pubmed']").get(XmlElement.class).flatMap(o -> o.getTextContent());
			} catch (XmlException e) {
				throw new RuntimeException(e);
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
				throw new RuntimeException(e);
			}
		}
		
		public Stream<MeshHeading> getMeshHeadings() {			
			try {
				return raw.doXpath(".//MeshHeading").getManyAsStream(XmlElement.class).map(o -> new MeshHeading(o));
			} catch (XmlException e) {
				throw new RuntimeException(e);
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
				throw new RuntimeException(e);
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
			return lastName()+"_"+firstName();
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
		
		private ELinkResult raw;
		public Links(ELinkResult raw) {this.raw	=raw; convert();}
		public ELinkResult raw() {return raw;}
		
		private List<Link> links;
		
		public Stream<Link> stream() {return links.stream();}
		
		private void convert() {
			links = new ArrayList<Link>();
			for (LinkSet ls: raw.getLinkSet()) {
				
				Optional<Id> idListId = ls.getIdListOrLinkSetDbOrLinkSetDbHistoryOrWebEnvOrIdUrlListOrIdCheckListOrERROR().stream()
						.filter(o -> o instanceof gov.nih.nlm.ncbi.eutils.generated.elink.IdList)
						.map(o -> (gov.nih.nlm.ncbi.eutils.generated.elink.IdList) o)
						.flatMap(idl -> idl.getId().stream()).findFirst();
				
				
				for (Object o: ls.getIdListOrLinkSetDbOrLinkSetDbHistoryOrWebEnvOrIdUrlListOrIdCheckListOrERROR()) {
					
					if (o instanceof LinkSetDb) {
						LinkSetDb lsd = ((LinkSetDb) o); 
						
						lsd.getLinkOrInfo().stream()
						.filter(o2 -> o2 instanceof gov.nih.nlm.ncbi.eutils.generated.elink.Link)
						.map(o2 -> (gov.nih.nlm.ncbi.eutils.generated.elink.Link) o2)
						.forEach(l -> links.add(
								new Link(ls,idListId.get(),lsd,l)
								));
						;
						
					} else if  (o instanceof IdUrlList) {
						
						for (IdUrlSet ius: ((IdUrlList) o).getIdUrlSet()) {
							
							ius.getObjUrlOrInfo().stream()
							.filter(o3 -> o3 instanceof ObjUrl)
							.map(o3 -> (ObjUrl) o3).forEach(
									ou -> links.add(new Link(ls,ius,ou))
									);
						}
					}
				}
			}
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
		
		protected Link(LinkSet linkSet, IdUrlSet idUrlSet, ObjUrl objUrl) {
			this.fromDb = linkSet.getDbFrom();
			this.fromId = idUrlSet.getId().getvalue();
			this.typeOrCategory = objUrl.getCategory().stream().findFirst().map(c -> c.getvalue());
			this.toDbOrUrl = objUrl.getUrl();
		}
		
		protected Link(LinkSet linkSet, Id fromId, LinkSetDb linkSetDb, gov.nih.nlm.ncbi.eutils.generated.elink.Link link) {
			this.fromDb = linkSet.getDbFrom();
			this.fromId = fromId.getvalue();
			this.typeOrCategory = Optional.of(linkSetDb.getLinkName());
			this.toDbOrUrl = linkSetDb.getDbTo();
			this.toId = Optional.of(link.getId().getvalue());
			this.score = Optional.ofNullable(link.getScore()).map(sc -> Long.parseLong(sc));
		}
		
		public String toString() {
			return fromDb+"\t"+fromId+"\t"+typeOrCategory.orElse("")+"\t"+toDbOrUrl+"\t"+toId.orElse("")+"\t"+score.orElse(0L);
		}
		
	}
}
