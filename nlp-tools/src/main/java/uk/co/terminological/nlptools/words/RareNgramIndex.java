package uk.co.terminological.nlptools.words;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.koloboke.collect.map.hash.HashIntIntMaps;

public class RareNgramIndex {

	Counter counts = new Counter(100000, 10000);
	
	
	public void update(List<Ngram> ngrams, Description desc) {
		ngrams.forEach(n -> counts.increment(n.index));
		
	}
	
	
	
	private class Counter {
		
		private int[] values;
		private int growth;
		
		public Counter(int initialSize, int growth) {
			values = new int[initialSize];
			this.growth = growth;
		}
		
		public void increment(int index) {
			if (index > values.length) {
				values = Arrays.copyOf(values, index+growth); 
			}
			values[index] += 1; 
		}
		
	}

}
