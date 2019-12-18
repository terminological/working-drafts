package uk.co.terminological.bibliography.client;

import java.util.Optional;

import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.Record;

public interface IdLocator {

	Optional<? extends Record> getById(IdType type, String id);
	
}
