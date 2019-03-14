package uk.co.terminological.nlptools.words;

public interface DictionaryEntry<X> {

	public Dictionary<? extends DictionaryEntry<X>> getDictionary();
	public X getValue();
	public long getIndex();
	
}
