package uk.co.terminological.pubmedclient;

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
import gov.nih.nlm.ncbi.eutils.generated.esearch.Count;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
import gov.nih.nlm.ncbi.eutils.generated.esearch.IdList;
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
				.filter(o -> (o instanceof IdList))
				.map(o -> (IdList) o)
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
}
