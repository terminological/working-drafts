package uk.co.terminological.pubmedclient;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubMedBatchQuery {
	
	private static Logger logger = LoggerFactory.getLogger(PubMedBatchQuery.class);
	private PubMedRestClient client;
	
	public static PubMedBatchQuery newSession(String apiKey, String appId, String developerEmail) {
		PubMedBatchQuery out = new PubMedBatchQuery();
		out.client = PubMedRestClient.create(apiKey, appId, developerEmail);
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
				logger.info("Searching from:{}, to:{} ", tmp.page.start,tmp.page.start+tmp.page.batchSize);
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
