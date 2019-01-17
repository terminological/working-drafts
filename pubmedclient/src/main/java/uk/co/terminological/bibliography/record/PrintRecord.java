package uk.co.terminological.bibliography.record;

import java.util.Optional;

public interface PrintRecord {

	public Optional<String> getFirstAuthorName();
	public Optional<String> getJournal();
	public Optional<String> getVolume();
	public Optional<String> getIssue();
	public Optional<Long> getYear();
	public Optional<String> getPage();
	
	public default String getLabel() {
		return getFirstAuthorName().orElse("Unknown")+" ("+getJournal().orElse("")
		+(getYear().isPresent() ? " "+getYear() : "") 
		+")";
	}
}
