package uk.co.terminological.bibliography;

public class BibliographicApiException extends RuntimeException {
	public BibliographicApiException(String string) {
		super(string);
	}

	public BibliographicApiException(String string, Exception e1) {
		super(string, e1);
	}
}