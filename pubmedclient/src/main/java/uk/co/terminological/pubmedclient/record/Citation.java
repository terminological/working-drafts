package uk.co.terminological.pubmedclient.record;

import java.util.Optional;

public interface Citation {

	Optional<RecordReference> getIdentifier();
	Optional<String> getTitle();
	Optional<PrintRecord> getBibliographicId();
	
}
