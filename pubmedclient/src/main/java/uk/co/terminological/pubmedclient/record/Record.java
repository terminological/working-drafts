package uk.co.terminological.pubmedclient.record;

import java.net.URI;
import java.time.LocalDate;
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
	public String getJournal();
	public Optional<URI> getPdfLocation(); 
	public Optional<LocalDate> getPublishedDate();
	
}
