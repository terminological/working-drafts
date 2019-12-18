package uk.co.terminological.bibliography.client;

import java.util.Collection;

import uk.co.terminological.bibliography.record.RecordIdentifierMapping;
import uk.co.terminological.bibliography.record.RecordReference;

public interface IdMapper {

	public Collection<? extends RecordIdentifierMapping> mappings(Collection<? extends RecordReference> source);
}
