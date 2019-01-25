package uk.co.terminological.nlptools;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Similarity {

	/*
	 * Calculates a significance of similarity based on the tfidf
	 * For every term in the source document in order of descending tfidf
	 * Find documents containing that term in the target corpus
	 * Calculate the contribution that term will have on an overall score metric and multiply that to the current score for every matching document
	 * Move onto next term.
	 * 
	 */
	public static <X> Double getEuclideanDistance(Stream<Weighted<X>> source, Stream<Weighted<X>> target) {
		
		HashMap<X,Double> tmp = new HashMap<>();
		
		// copy first stream to vector
		source.forEach(kv -> tmp.put(kv.getTarget(), kv.getWeight()));
		// subtract second stream pairwise
		target.forEach(kv2 -> tmp.merge(kv2.getTarget(), -kv2.getWeight(), (v1,v2) -> v1-v2));
		
		Double subSquares = tmp.entrySet().stream().collect(Collectors.summingDouble(kv -> kv.getValue()*kv.getValue()));
		return Math.sqrt(subSquares);
	}
	
	/*
	 * Calculates a cosine similarity based on a scored 
	 * For every term in the source document in order of descending tfidf
	 * Find documents containing that term in the target corpus
	 * Calculate the contribution that term will have on an overall score metric and multiply that to the current score for every matching document
	 * Move onto next term.
	 * 
	 */
	public static <X> Double getCosineDifference(Stream<Weighted<X>> source, Stream<Weighted<X>> target) {
		
		HashMap<X,Double> tmpSource = new HashMap<>();
		HashMap<X,Double> tmpTarget = new HashMap<>();
		
		Double sourceLengthSqrd = source.collect(Collectors.summingDouble(v -> {
			tmpSource.put(v.getTarget(), v.getWeight());
			return v.getWeight()*v.getWeight();
		})); 
		
		Double targetLengthSqrd = target.collect(Collectors.summingDouble(v -> {
			tmpTarget.put(v.getTarget(), v.getWeight());
			return v.getWeight()*v.getWeight();
		}));
		
		Double dotProd = tmpSource.entrySet().stream().collect(Collectors.summingDouble(kv -> 
			kv.getValue()*tmpTarget.getOrDefault(kv.getKey(),0D)
		));
		
		return 1 - dotProd / (Math.sqrt(sourceLengthSqrd*targetLengthSqrd));
	}
	
}
