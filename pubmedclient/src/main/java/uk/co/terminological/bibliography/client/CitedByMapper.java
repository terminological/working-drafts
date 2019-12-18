package uk.co.terminological.bibliography.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uk.co.terminological.bibliography.record.CitationLink;
import uk.co.terminological.bibliography.record.RecordIdentifier;
import uk.co.terminological.bibliography.record.RecordReference;

public interface CitedByMapper {
	
	public Map<? extends RecordIdentifier, Collection<? extends CitationLink>> referencesCiting(Collection<RecordReference> ref); 
	
	
	default Collection<? extends CitationLink> referencesCiting(RecordReference ref) {
		return referencesCiting(Collections.singleton(ref)).get(ref);
	};
	
}
