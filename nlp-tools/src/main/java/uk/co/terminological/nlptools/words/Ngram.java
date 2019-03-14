package uk.co.terminological.nlptools.words;

public class Ngram {

	int index;
	
	
	public Ngram(int int1) {
		index = int1;
	}


	public String getValue(NGramDictionary dict) {
		return dict.lookup(index);
	}

}
