package uk.co.terminological.nlptools.words;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RareNgramIndex {

	Counter counts = new Counter(100000, 10000);
	
	
	public void update(List<Ngram> ngrams, Description desc) {
		//Map<Ngram,Mapping> freqs = new HashMap<>();
		for (int seq = 0; seq < ngrams.size(); seq++) {
			Ngram n = ngrams.get(seq);
			int prevOrder = counts.value(n.index);
			Mapping m = new Mapping(
					counts.increment(n.index),
					seq,
					desc.getId());
			
		}
		/*freqs
			.entrySet()
			.stream()
			
			.forEach(kv -> {
				Ngram ngram = kv.getKey();
				int currentCount = kv.getValue();
				int sequence = ngrams.indexOf(o)
			});*/
		
	}
	
	
	private class Mapping {
		public Mapping(int increment, int seq, int id) {
			this.count = increment;
			this.sequence = seq;
			this.descId =id;
		}
		int count;
		int sequence;
		int descId;
	}
	
	private class Counter {
		
		private int[] values;
		private int growth;
		
		public Counter(int initialSize, int growth) {
			values = new int[initialSize];
			this.growth = growth;
		}
		
		public int value(int index) {
			return values[index];
		}
		
		public int increment(int index) {
			if (index > values.length) {
				values = Arrays.copyOf(values, index+growth); 
			}
			values[index] += 1;
			return values[index]; 
		}
		
	}

}
