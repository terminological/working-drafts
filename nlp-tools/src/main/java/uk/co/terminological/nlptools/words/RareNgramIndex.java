package uk.co.terminological.nlptools.words;

import java.util.Map;

import com.koloboke.collect.map.hash.HashIntIntMaps;

public class RareNgramIndex {

	Map<Integer,Integer> counts = HashIntIntMaps.newMutableMap(100000);
	
	public void update(Ngram ngram) {
		counts.put(
				ngram.index,
				counts.getOrDefault(ngram.index, 0)+1);
	}
	
	public void addDescription(Description desc) {
		//TODO: create ngrams for description 
	}

}
