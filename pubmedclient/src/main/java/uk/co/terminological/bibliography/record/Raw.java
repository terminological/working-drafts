package uk.co.terminological.bibliography.record;

/**
 * provides access to the low level response of a bibliographic api
 */
public interface Raw<X> {
	X getRaw();
}
