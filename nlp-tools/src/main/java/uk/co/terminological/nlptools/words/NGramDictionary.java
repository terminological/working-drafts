package uk.co.terminological.nlptools.words;

import java.util.List;
import java.util.Map;

import com.koloboke.collect.map.ObjIntMap;
import com.koloboke.collect.map.ObjIntMapFactory;
import com.koloboke.collect.map.hash.HashObjIntMaps;

public class NGramDictionary {

	Map<String,Integer> map = HashObjIntMaps.newMutableMap(100000);
	List<String> strings;
	RareNgramIndex index;
	
	public String lookup(int index) {
		return strings.get(index);
	}
	
	public Ngram create(String characters) {
		if (map.containsKey(characters)) {
			Ngram ngram = new Ngram(map.get(characters));
			index.update(ngram);
			return ngram;
		} else {
			
		}
			
	};
	
}
