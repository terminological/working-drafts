package uk.co.terminological.pubmedclient;

import java.util.ArrayList;
import java.util.List;

public class PubMedBatch {
	
	private PubMedBatchQuery.Page page;
	private PubMedBatchQuery query;
	private String queryString;
	
	protected PubMedBatch(PubMedBatchQuery query, String queryString, PubMedBatchQuery.Page page) {
		this.query = query;
		this.page = page;
		this.queryString = queryString;
		
	}
	
}
