package uk.co.terminological.pubmedclient.record;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Record extends RecordReference {

	public RecordReference getPrimaryIdentifier();
	public Set<RecordReference> getOtherIdentifiers();
	public List<Author> getAuthors();
	public Optional<String> getAbstract();
	public String getTitle();
	public List<RecordReference> getCitations();
	public String getJournalLocation();
	public Optional<URL> getPdfLocation(); 
	public Optional<LocalDate> getPublishedDate();
	
}
