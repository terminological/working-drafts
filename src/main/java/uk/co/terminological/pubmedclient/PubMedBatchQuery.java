package uk.co.terminological.pubmedclient;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class PubMedBatchQuery {
	
	private PubMedRestClient client;
	
	public static PubMedBatchQuery newSession(String apiKey, String appId, String developerEmail) {
		PubMedBatchQuery out = new PubMedBatchQuery();
		out.client = new PubMedRestClient(apiKey, appId, developerEmail);
		return out;
	}
	
	public PubMedBatch search(String searchQuery) {
		return search(searchQuery, Page.basic());
	}
	
	public PubMedBatch search(String searchQuery, Page paging) {
		return new PubMedBatch(this, searchQuery, paging);
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
		
	}
}
