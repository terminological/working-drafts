package uk.co.terminological.pubmedclient.record;

import java.util.Optional;
import java.util.stream.Stream;

public interface Author {

	Optional<String> getORCID();
	Optional<String> getFirstName();
	String getLastName();
	Optional<String> getInitials();
	Stream<String> getAffiliations();
	
}
