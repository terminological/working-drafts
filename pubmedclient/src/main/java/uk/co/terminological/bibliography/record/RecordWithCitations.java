package uk.co.terminological.bibliography.record;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.terminological.bibliography.crossref.Reference;

public interface RecordWithCitations extends Record {

	public Stream<? extends Reference> getCitations();
	public Optional<Long> getCitedByCount();
	public Optional<Long> getReferencesCount();
	 		
}
