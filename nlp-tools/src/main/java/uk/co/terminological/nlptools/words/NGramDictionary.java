package uk.co.terminological.nlptools.words;

import java.util.List;
import java.util.Map;

public class NGramDictionary {

	StringIntMap map = StringIntMap.withExpectedSize(100000);
	List<String> strings;
	RareNgramIndex index;
	
	public String lookup(int index) {
		return strings.get(index);
	}
	
	public Ngram create(String characters) {
		if (map.containsKey(characters)) {
			Ngram ngram = new Ngram(map.getInt(characters));
			index.update(ngram);
			
		} else {
			
		}
			
	};
	
}
