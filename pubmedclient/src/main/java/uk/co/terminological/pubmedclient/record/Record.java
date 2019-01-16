package uk.co.terminological.pubmedclient.record;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Record extends RecordReference {

	public Set<RecordReference> getOtherIdentifiers();
	
	public Stream<? extends Author> getAuthors();
	public Stream<String> getLicenses();
	
	public Optional<String> getAbstract();
	public String getTitle();
	public String getJournal();
	public Optional<LocalDate> getDate();
		
	public Stream<? extends RecordReference> getCitations();
	public Optional<URI> getPdfUri(); 
	
}
