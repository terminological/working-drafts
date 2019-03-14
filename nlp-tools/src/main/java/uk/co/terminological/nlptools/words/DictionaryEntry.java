package uk.co.terminological.nlptools.words;

public interface DictionaryEntry<X> {

	public Dictionary<? extends DictionaryEntry<X>,X> getDictionary();
	public X getValue();
	public default long getIndex() {
		return getDictionary().lookup(getValue());
	};
	
}
