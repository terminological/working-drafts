package uk.co.terminological.bibliography.client;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import uk.co.terminological.bibliography.record.Record;

public interface Searcher {

	Collection<? extends Record> search(String search, Optional<Date> from,  Optional<Date> to, Optional<Integer> limit);
	
}
