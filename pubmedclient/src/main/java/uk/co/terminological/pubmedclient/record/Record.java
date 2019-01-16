package uk.co.terminological.pubmedclient.record;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Record {

	public RecordReference getPrimaryIdentifier();
	public Set<RecordReference> getOtherIdentifiers();
	
	public Stream<Author> getAuthors();
	public Stream<String> getLicenses();
	
	public Optional<String> getAbstract();
	public String getTitle();
	public String getJournal();
	public Optional<LocalDate> getDate();
		
	public Stream<RecordReference> getCitations();
	public Optional<URI> getPdfUri(); 
	
}
