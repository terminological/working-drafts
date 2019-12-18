package uk.co.terminological.bibliography.client;

import java.util.Collection;

import uk.co.terminological.bibliography.record.CitationLink;
import uk.co.terminological.bibliography.record.RecordReference;

public interface CitedByMapper {
	
	public Collection<? extends CitationLink> cites(RecordReference ref);
	
}
