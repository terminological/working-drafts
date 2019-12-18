package uk.co.terminological.bibliography.client;

import java.util.Collection;

import uk.co.terminological.bibliography.record.Record;

public interface Searcher {

	Collection<? extends Record> search(String search, int limit);
	
}
