package uk.co.terminological.pubmedclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.nih.nlm.ncbi.eutils.generated.efetch.Author;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.ELinkResult;
import gov.nih.nlm.ncbi.eutils.generated.elink.Id;
import gov.nih.nlm.ncbi.eutils.generated.elink.IdUrlList;
import gov.nih.nlm.ncbi.eutils.generated.elink.IdUrlSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.LinkSet;
import gov.nih.nlm.ncbi.eutils.generated.elink.LinkSetDb;
import gov.nih.nlm.ncbi.eutils.generated.elink.ObjUrl;
import gov.nih.nlm.ncbi.eutils.generated.esearch.Count;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;

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

		private PubmedArticleSet raw;
		
		public PubMedEntries(PubmedArticleSet raw) {this.raw = raw;}
		
		public Stream<PubMedEntry> stream() {
			return raw.getPubmedArticleOrPubmedBookArticle().stream()
					.filter(o->o instanceof PubmedArticle).map(o-> new PubMedEntry((PubmedArticle) o));
			
		}
		
		public List<String> getTitles() {
			return this.stream().map(e -> e.getTitle()).collect(Collectors.toList());
		}
		
	}
	
	public static class PubMedEntry {
		
		private PubmedArticle raw;
		
		public PubMedEntry(PubmedArticle raw) {this.raw = raw;}
		
		public PubmedArticle getRaw() {return raw;}
		
		public String getTitle() {
			return raw.getMedlineCitation().getArticle().getArticleTitle().getvalue();
		}
		
		public Optional<String> getDoi() {
			return Optional.ofNullable(raw.getPubmedData().getArticleIdList()).stream()
					.flatMap(o -> o.getArticleId().stream())
					.filter(aid -> aid.getIdType().equals("doi")).findFirst().map(aid -> aid.getvalue());
		}
		
		public Optional<String> getPMCID() {
			return 
					Optional.ofNullable(raw.getPubmedData().getArticleIdList()).stream()
					.flatMap(o -> o.getArticleId().stream())
					.filter(aid -> aid.getIdType().equals("pmc")).findFirst().map(aid -> aid.getvalue());
		}
		
		public List<String> getMeshHeadings() {
			return 
					Optional.ofNullable(raw.getMedlineCitation()).stream()
					.flatMap(o -> Optional.ofNullable(o.getMeshHeadingList()).stream())
					.flatMap(o -> o.getMeshHeading().stream())
					.map(mh -> mh.getDescriptorName().getvalue())
					.collect(Collectors.toList());
		}

		public String getAbstract() {return getAbstract(true);}
		public String getUnlabelledAbstract() {return getAbstract(false);}
		
		private String getAbstract(boolean labelled) {
			return raw.getMedlineCitation().getArticle().getAbstract()
					.getAbstractText().stream()
					.map(at -> 
						(labelled && at.getLabel() != null?at.getLabel()+"\t":"")+at.getvalue())
					.collect(Collectors.joining("\n"));
		}

		public String getPMID() {
			return raw.getPubmedData().getArticleIdList().getArticleId().stream()
					.filter(aid -> aid.getIdType().equals("pubmed")).findFirst()
					.map(aid -> aid.getvalue()).get();
		}
		
		public List<Author> getAuthors() {
			return Optional.ofNullable(raw.getMedlineCitation()).stream()
					.flatMap(o -> Optional.ofNullable(o.getArticle()).stream())
					.flatMap(o -> Optional.ofNullable(o.getAuthorList()).stream())
					.map(o -> o.getAuthor())
					.findFirst()
					.orElse(Collections.emptyList());
		}
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
		String fromDb;
		String fromId;
		Optional<String> typeOrCategory = Optional.empty();
		Optional<Long> score = Optional.empty();
		String toDbOrUrl;
		Optional<String> toId = Optional.empty();
		
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
