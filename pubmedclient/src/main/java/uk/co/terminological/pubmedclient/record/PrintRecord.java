package uk.co.terminological.pubmedclient.record;

import java.util.Optional;

public interface PrintRecord {

	public String getFirstAuthorName();
	public String getJournal();
	public Optional<String> getVolume();
	public Optional<String> getIssue();
	public Optional<Long> getYear();
	public Optional<String> getPage();
}
