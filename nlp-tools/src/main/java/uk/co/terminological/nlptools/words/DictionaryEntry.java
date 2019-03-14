package uk.co.terminological.nlptools.words;

public interface DictionaryEntry<X> {

	public Dictionary<DictionaryEntry<X>,X> getDictionary();
	public X getValue();
	public long getIndex();
	
}
