package uk.co.terminological.pubmedclient.record;

import java.util.Optional;
import java.util.stream.Stream;

public interface RecordWithCitations extends Record {

	public Stream<? extends RecordReference> getCitations();
	public Optional<Long> getCitedByCount();
	public Long getReferencesCount();
	 		
}
