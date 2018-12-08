package uk.co.terminological.pubmedclient.record;

import java.util.Optional;

public interface Author {

	Optional<RecordReference> getOrcid();
	Optional<String> getFirstName();
	String getLastName();
	Optional<String> getInitials();
	Optional<Affiliation> getAffiliation();
	
}
