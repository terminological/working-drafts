package uk.co.terminological.pubmedclient.record;

import java.util.stream.Stream;

public interface RecordWithCitations extends Record {

	public Stream<? extends RecordReference> getCitations();
	
}
