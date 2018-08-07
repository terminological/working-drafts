package uk.co.terminological.pubmedclient;

import java.util.Iterator;

public class PubMedBatchQuery {
	
	private PubMedRestClient client;
	
	public static PubMedBatchQuery newSession(String apiKey, String appId, String developerEmail) {
		PubMedBatchQuery out = new PubMedBatchQuery();
		out.client = new PubMedRestClient(apiKey, appId, developerEmail);
		return out;
	}
	
	public Iterator<PubMedBatch> search(String searchQuery) {
		return search(searchQuery, Page.basic());
	}
	
	public Iterator<PubMedBatch> search(String searchQuery, Page paging) {
		return new Iterator<PubMedBatch>() { 
			
			PubMedBatch tmp = new PubMedBatch(PubMedBatchQuery.this, searchQuery, paging);

			@Override
			public boolean hasNext() {
				return tmp.page.start+tmp.page.batchSize < tmp.resultSize ;
			}

			@Override
			public PubMedBatch next() {
				return new PubMedBatch(tmp.query, tmp.queryString, tmp.page.advance());
			}
		};
	}
	
	protected PubMedRestClient getClient() {return client;}
	
	public static class Page {
		protected int start = 0;
		protected int batchSize = 50;
		
		public static Page basic() {
			return new Page();
		}
		
		public Page withStart(int start) {
			this.start = start;
			return this;
		}
		
		public Page withSize(int size) {
			this.batchSize = size;
			return this;
		}
		
		public Page advance() {
			this.start = start+batchSize;
			return this;
		}
		
	}
	
	
}
