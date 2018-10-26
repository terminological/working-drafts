package uk.co.terminological.pubmedclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
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

import uk.co.terminological.datatypes.IterableMapper;

public class PubMedResult {

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
	
	public static class EntrySet {

		private PubmedArticleSet raw;
		
		public EntrySet(PubmedArticleSet raw) {this.raw = raw;}
		
		public Stream<Entry> stream() {
			return raw.getPubmedArticleOrPubmedBookArticle().stream()
					.filter(o->o instanceof PubmedArticle).map(o-> new Entry((PubmedArticle) o));
			
		}
		
		public List<String> getTitles() {
			return this.stream().map(e -> e.getRaw())
						.map(a -> a.getMedlineCitation().getArticle().getArticleTitle().getvalue())
						.collect(Collectors.toList());
		}
		
	}
	
	public static class Entry {
		
		private PubmedArticle raw;
		
		public Entry(PubmedArticle raw) {this.raw = raw;}
		
		public PubmedArticle getRaw() {return raw;}
		
		public Optional<String> getDoiFromPubmedArticle() {
			return raw.getPubmedData()
					.getArticleIdList().getArticleId().stream()
					.filter(aid -> aid.getIdType().equals("doi")).findFirst().map(aid -> aid.getvalue());
		}
		
		public Optional<String> getPMCIDFromPubmedArticle() {
			return raw.getPubmedData()
					.getArticleIdList().getArticleId().stream()
					.filter(aid -> aid.getIdType().equals("pmc")).findFirst().map(aid -> aid.getvalue());
		}
		
		public MeshHeadingList getMeshHeadings() {
			return raw.getMedlineCitation().getMeshHeadingList();
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
	
	public static class Link {
		String fromDb;
		String fromId;
		Optional<String> typeOrCategory = Optional.empty();
		Optional<Long> score = Optional.empty();
		String toDbOrUrl;
		Optional<String> toId = Optional.empty();
		
		public Link(LinkSet linkSet, IdUrlSet idUrlSet, ObjUrl objUrl) {
			this.fromDb = linkSet.getDbFrom();
			this.fromId = idUrlSet.getId().getvalue();
			this.typeOrCategory = objUrl.getCategory().stream().findFirst().map(c -> c.getvalue());
			this.toDbOrUrl = objUrl.getUrl();
		}
		
		public Link(LinkSet linkSet, Id fromId, LinkSetDb linkSetDb, gov.nih.nlm.ncbi.eutils.generated.elink.Link link) {
			this.fromDb = linkSet.getDbFrom();
			this.fromId = fromId.getvalue();
			this.typeOrCategory = Optional.of(linkSetDb.getLinkName());
			this.toDbOrUrl = linkSetDb.getDbTo();
			this.toId = Optional.of(link.getId().getvalue());
		}
		
	}
}
