package uk.co.terminological.pubmedclient;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.*;
import gov.nih.nlm.ncbi.eutils.generated.esummary.ESummaryResult;

public class PubMedBatch implements Iterator<PubMedBatch> {
	
	private PubMedBatchQuery.Page page;
	private PubMedBatchQuery query;
	private String queryString;
	private ESearchResult search;
	private ESummaryResult summary;
	private PubmedArticleSet articles;
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
			throw new RuntimeException("Some sort of API parsing error has occurred",e);
		}
	}
	
	public List<String> getTitles() {
		if (articles != null) {
			articles.getPubmedArticleOrPubmedBookArticle().stream()
				.filter(a -> a instanceof PubmedArticle).map(a -> (PubmedArticle) a)
				.map(a -> a.)
		}
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
