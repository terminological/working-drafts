package uk.co.terminological.bibliography.record;

import java.util.stream.Stream;

import uk.co.terminological.bibliography.opencitations.OpenCitationsCitation;

public interface CitationSource {

	public Stream<OpenCitationsCitation> getCitations();
	
}
