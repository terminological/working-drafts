package uk.co.terminological.nlptools.words;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.koloboke.collect.map.ObjIntMap;
import com.koloboke.collect.map.ObjIntMapFactory;
import com.koloboke.collect.map.hash.HashObjIntMaps;

import uk.co.terminological.nlptools.Normaliser;

public class NGramDictionary {

	Map<String,Integer> map = HashObjIntMaps.newMutableMap(100000);
	List<String> strings = new ArrayList<>();
	Normaliser norm;
	RareNgramIndex index;
	int nGramSize;
	
	public String lookup(int index) {
		return strings.get(index);
	}
	
	protected String ngramTerm(String term) {
		return new String(new char[nGramSize-1])+
				norm.apply(term)+
				new String(new char[nGramSize-1]);
	}
	
	public List<Ngram> create(Description desc) {
		List<Ngram> out = new ArrayList<>();
	
		String tmp = ngramTerm(desc.getTerm());
		for (int pos = 0; pos<(tmp.length()-nGramSize); pos++) {
			String characters = tmp.substring(pos, pos+nGramSize);
			Ngram ngram = null;
			if (map.containsKey(characters)) {
				ngram = new Ngram(map.get(characters));
			} else {
				int key = strings.size();
				map.put(characters, key);
				strings.add(characters);
				ngram = new Ngram(key);
			}
			
			out.add(ngram);
		}
		
		index.update(out, desc);
		
		return out;
	};
	
}
