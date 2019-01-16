package uk.co.terminological.pubmedclient.record;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Record {

	public String getIdentifier();
	public IdType getIdentifierType();
	public RecordReference getPrimaryIdentifier();
	public Set<RecordReference> getOtherIdentifiers();
	public Stream<Author> getAuthors();
	public Optional<String> getAbstract();
	public String getTitle();
	public Stream<RecordReference> getCitations();
	public String getJournalLocation();
	public Optional<URI> getPdfLocation(); 
	public Optional<LocalDate> getPublishedDate();
	
}
