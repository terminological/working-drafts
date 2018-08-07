package uk.co.terminological.pubmedclient;

import java.util.Iterator;

import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticleSet;
import gov.nih.nlm.ncbi.eutils.generated.esearch.ESearchResult;
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
