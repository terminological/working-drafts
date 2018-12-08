package uk.co.terminological.pubmedclient.record;

import java.util.Set;

public interface Record extends RecordReference {

	public Set<RecordReference> getOtherIdentifiers();
	
}
