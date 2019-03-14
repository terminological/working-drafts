package uk.co.terminological.nlptools.words;

public interface DictionaryEntry<X> {

	public Dictionary<? extends DictionaryEntry<X>,X> getDictionary();
	public default X getValue() {
		return getDictionary().lookup(getIndex());
	};
	public default long getIndex();
	
}
