package uk.co.terminological.bibliography.client;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import uk.co.terminological.bibliography.record.Record;

public interface Searcher {

	Collection<? extends Record> search(String search, Optional<Date> from,  Optional<Date> to, Optional<Integer> limit);
	
	default Collection<? extends Record> search(String search) {
		return search(search, Optional.empty(), Optional.empty(), Optional.empty());
	};
	
	default Collection<? extends Record> search(String search, Integer limit) {
		return search(search, Optional.empty(), Optional.empty(), Optional.of(limit));
	};
	
	default Collection<? extends Record> search(String search, Date from, Date to) {
		return search(search, Optional.of(from), Optional.of(to), Optional.empty());
	};
	
	
	
}
