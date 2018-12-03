package uk.co.terminological.nlptools;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Similarity {

	/*
	 * Calculates a significance of similarity based on the tfidf
	 * For every term in the source document in order of descending tfidf
	 * Find documents containing that term in the target corpus
	 * Calculate the contribution that term will have on an overall score metric and multiply that to the current score for every matching document
	 * Move onto next term.
	 * 
	 */
	public static Double getEuclideanDistance(Map<Term,Double> source, Map<Term,Double> target) {
		
		HashMap<Term,Double> targetTerms = new HashMap<>(target);
		
		source.forEach((k,v) -> {
			Optional.ofNullable(targetTerms.get(k)).ifPresentOrElse(
					tv -> targetTerms.put(k, tv-v), 
					() -> targetTerms.put(k, -v));
		});
		
		Double subSquares = targetTerms.entrySet().stream().collect(Collectors.summingDouble(kv -> kv.getValue()*kv.getValue()));
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
	public static Double getCosineDifference(Map<Term,Double> source, Map<Term,Double> target) {
		
		Double dotProd = source.entrySet().stream().collect(Collectors.summingDouble(kv -> 
			kv.getValue()*target.getOrDefault(kv.getKey(),0D)
		));
		
		Double sourceLengthSqrd = source.values().stream().collect(Collectors.summingDouble(v -> v*v)); 
		Double targetLengthSqrd = target.values().stream().collect(Collectors.summingDouble(v -> v*v));
		
		return 1 - dotProd / (Math.sqrt(sourceLengthSqrd*targetLengthSqrd));
	}
	
}
