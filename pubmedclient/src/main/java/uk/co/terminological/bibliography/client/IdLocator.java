package uk.co.terminological.bibliography.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.Record;
import uk.co.terminological.bibliography.record.RecordReference;

public interface IdLocator {

	Optional<? extends Record> getById(Collection<RecordReference> equivalentIds);
	
	default Optional<? extends Record> getById(IdType type, String id) {
		return getById(Collections.singleton(new RecordReference() {

			@Override
			public Optional<String> getIdentifier() {
				return Optional.of(id);
			}

			@Override
			public IdType getIdentifierType() {
				return type;
			}}));
	};
	
	default Optional<? extends Record> getById(Record rec) {
		Set<RecordReference> tmp = new HashSet<>();
		tmp.add(rec);
		tmp.addAll(rec.getOtherIdentifiers());
		return getById(tmp);
	};
	
}
