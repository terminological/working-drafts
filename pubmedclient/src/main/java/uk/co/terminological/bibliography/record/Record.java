package uk.co.terminological.bibliography.record;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface Record extends RecordReference {

	public Set<RecordReference> getOtherIdentifiers();
	
	public default Optional<? extends Author> getFirstAuthor() {
		return getAuthors().findFirst();
	};
	
	public Stream<? extends Author> getAuthors();
	public Stream<String> getLicenses();
	
	public Optional<String> getAbstract();
	public Optional<String> getTitle();
	public Optional<String> getJournal();
	public Optional<LocalDate> getDate();
		
	public Optional<URI> getPdfUri();
	
	public default Optional<String> getFirstAuthorLastName() {
		return getFirstAuthor().map(a -> a.getLastName());
	}
	
	public default Optional<String> getFirstAuthorFirstName() {
		return getFirstAuthor().flatMap(a -> a.getFirstName());
	}
	
}
