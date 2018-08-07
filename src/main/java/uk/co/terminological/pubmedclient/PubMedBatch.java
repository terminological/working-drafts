package uk.co.terminological.pubmedclient;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.*;
import gov.nih.nlm.ncbi.eutils.generated.esummary.ESummaryResult;

public class PubMedBatch implements Iterator<PubMedBatch> {
	
	private PubMedBatchQuery.Page page;
	private PubMedBatchQuery query;
	private String queryString;
	private ESearchResult search;
	private List<PubmedArticle> articles;
	private int resultSize = 0;
	
	protected PubMedBatch(PubMedBatchQuery query, String queryString, PubMedBatchQuery.Page page) {
		this.query = query;
		this.page = page;
		this.queryString = queryString;
		try {
			this.search = query.getClient().searchPubmed(queryString, page.start, page.batchSize);
			this.resultSize = search.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR().stream()
					.filter(a -> a instanceof Count).map(a -> Integer.parseInt(((Count) a).getvalue()))
					.findFirst().orElse(0);
		} catch (JAXBException e) {
			throw new RuntimeException("Some sort of API parsing error has occurred getting search results",e);
		}
	}
	
	public List<String> idsFromSearch() {
		return search.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR().stream()
				.filter(a -> a instanceof IdList).map(a -> (IdList) a)
				.flatMap(list -> list.getId().stream())
				.map(id -> id.getvalue())
				.collect(Collectors.toList());
	}
	
	public List<String> getTitles() {
		if (articles == null) {
			try {
				articles = query.getClient().fetchPubmedArticle(idsFromSearch());
			} catch (JAXBException e) {
				throw new RuntimeException("Some sort of API parsing error has occurred getting articles",e);				
			}
		}
		return articles.stream()
				.map(a -> a.getMedlineCitation().getArticle().getArticleTitle().getvalue())
				.collect(Collectors.toList());
				
	}
	
	@Override
	public boolean hasNext() {
		return page.start+page.batchSize < resultSize ;
	}

	@Override
	public PubMedBatch next() {
		return new PubMedBatch(query, queryString, page.withStart(page.start+page.batchSize));
	}
}
