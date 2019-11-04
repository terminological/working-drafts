package uk.co.terminological.bibliography.record;

import java.util.stream.Stream;

import uk.co.terminological.bibliography.opencitations.Citation;

public interface CitationSource {

	public Stream<Citation> getCitations()
	
}
