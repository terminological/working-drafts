package uk.co.terminological.nlptools;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.similarity.SimilarityScore;

public class StringCrossMapper {

	Map<String,Document> sources = new LinkedHashMap<>();
	Map<String,Document> targets = new HashMap<>();
	Corpus sourceCorpus;
	Corpus targetCorpus;
	Normaliser normaliser;
	Tokeniser tokeniser;
	
	public static class DuplicateIdentityException extends RuntimeException {}
	
	public void addSource(String id, String source) {
		if (sources.containsKey(id)) throw new DuplicateIdentityException();
		this.sources.put(id, new Document(id, source, sourceCorpus));
	}
	
	public Corpus getSource() {return sourceCorpus;}
	public Corpus getTarget() {return targetCorpus;}
	
	public void addTarget(String id, String target) {
		if (targets.containsKey(id)) throw new DuplicateIdentityException();
		this.targets.put(id, new Document(id, target, targetCorpus));
	}
	
	public StringCrossMapper(String... stopWords) {
		this(
			string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^a-zA-Z0-9\\s]", "-").replaceAll("\\s+", " ").toLowerCase(),
			string -> Stream.of(string.split("\\s+")).filter(s -> !s.equals("-")),
			stopWords
		);
	}
	
	public StringCrossMapper(Normaliser normaliser, Tokeniser tokeniser, String[] stopWords) {
		this.normaliser = normaliser;
		this.tokeniser = tokeniser;
		sourceCorpus = new Corpus(normaliser, tokeniser, stopWords);
		targetCorpus = new Corpus(normaliser, tokeniser, stopWords);
	}
	
	/**
	 * looks for documents which share terms with high tfidf scores 
	 * @param doc
	 * @return
	 */
	public Map<Document,Document> getBestMatches() {
		Map<Document,Document> match = new HashMap<>();
 		for (Entry<String,Document> source: sources.entrySet()) {
			getBestMatch(source.getValue()).ifPresent(
					doc2 -> match.put(source.getValue(), doc2)); 
		}
 		return match;
	}
	
	/*
	 * looks for documents which share terms with high tfidf score 
	 */
	private Optional<Document> getBestMatch(Document doc) {
		Map<Term,Double> orderedTerms = doc.tfIdfsDescending();
		Iterator<Entry<Term, Double>> it = orderedTerms.entrySet().iterator();
		
		Set<Document> matching = targetCorpus.getDocuments();
		
		double score = 0;
		while (it.hasNext() && matching.size() > 1) {
			Entry<Term, Double> next = it.next();
			Term nextTerm = next.getKey();
			Term outputTerm = targetCorpus.getTermFrom(nextTerm.tag);
			Set<Document> tmp = new HashSet<>(outputTerm.getDocumentsUsing());
			tmp.retainAll(matching); 
			if (tmp.size() > 0) {
				matching = tmp;
				score += next.getValue();
			}
		}
		
		if (matching.size() == 1) return matching.stream().findFirst();
		if (score == 0) return Optional.empty();
		Document bestMatch = null;
		
		Double bestScore = Double.NEGATIVE_INFINITY;
		for (Document match: matching) {
			
			Map<Term, Double> found = new HashMap<>(match.tfIdfsDescending());
			Map<Term, Double> orig = new HashMap<>(doc.tfIdfsDescending());
			
			Double tmpScore = 0D;
			
			tmpScore += orig.entrySet().stream().collect(Collectors.summingDouble(
				kv -> Optional.ofNullable(found.get(kv.getKey())).orElse(-kv.getValue())
			));
			
			tmpScore += found.entrySet().stream().collect(Collectors.summingDouble(
				kv -> Optional.ofNullable(orig.get(kv.getKey())).orElse(-kv.getValue())
			));
			
			if (tmpScore > bestScore) {
				bestScore = tmpScore;
				bestMatch = match;
			}
			
		}
		
		return Optional.ofNullable(bestMatch);
		
	}
	
	/**
	 * 
	 * @param minValue
	 * @return
	 */
	public Map<Document,Map<Document,Double>> getAllMatchesBySignificance(Double minValue) {
		Map<Document,Map<Document,Double>> match = new HashMap<>();
 		
 		sourceCorpus.getDocuments().forEach(doc -> {
 			match.put(
 				doc, 
 				getAllMatchesBySignificance(doc)
 					.filter(kv -> kv.getValue() > minValue)
 					.collect(
 						Collectors.toMap(
 							kv -> kv.getKey(), 
 							kv -> kv.getValue(),
 							(e1, e2) -> e1, 
 			                LinkedHashMap::new
 						))); 
 		});
 		
 		return match;
	}
	
	/*
	 * Calculates a significance of similarity based on the tfidf
	 * For every term in the source document in order of descending tfidf
	 * Find documents containing that term in the target corpus
	 * Calculate the contribution that term will have on an overall score metric and multiply that to the current score for every matching document
	 * Move onto next term.
	 * 
	 */
	private Stream<Entry<Document,Double>> getAllMatchesBySignificance(Document doc) {
		
		Map<Document,Double> output = new HashMap<>();
		
		doc.tfIdfsDescending().forEach((nextTerm,docTfidfScore) -> {
			
			Optional<Term> optOutputTerm = targetCorpus.getMatchingTerm(nextTerm);
			optOutputTerm.ifPresent(outputTerm -> {
				outputTerm.getDocumentsUsing().forEach(matchingDoc -> {
					Optional.ofNullable(output.get(matchingDoc)).ifPresentOrElse(
							scoreSoFar -> output.put(matchingDoc, 
									scoreSoFar+docTfidfScore*matchingDoc.tfIdf(outputTerm)), 
							() -> output.put(matchingDoc, 
									docTfidfScore*matchingDoc.tfIdf(outputTerm)));
				});
			});
		});
		
		return output.entrySet()
         .stream()
         .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()));
	}
	
	/**
	 * Computes the similarity for all combinations of source and target base on the raw text of the document, excluding normalisations
	 * @param minValue
	 * @param metric a class from org.apache.commons.text.similarity
	 * @return
	 */
	public <K extends Comparable<K>> Map<Document,Map<Document,K>> getAllMatchesByDistance(K minValue, SimilarityScore<K> metric) {
		Map<Document,Map<Document,K>> match = new HashMap<>();
 		for (Document doc: sources.values()) {
 			match.put(
 					doc, 
 					getAllMatchesByDistance(doc, metric)
 					.filter(kv -> kv.getValue().compareTo(minValue) >= 0)
 					.collect(
 							Collectors.toMap(
 									kv -> kv.getKey(), 
 									kv -> kv.getValue(),
 									(e1, e2) -> e1, 
 					                LinkedHashMap::new
 									))); 
		}
 		return match;
	}
	
	/*
	 * Takes an input document and calculates a similarity score for all the target documents based on the raw text of the document.
	 */
	private <K extends Comparable<K>> Stream<Entry<Document,K>> getAllMatchesByDistance(Document doc, SimilarityScore<K> similarity) {
		Map<Document,K> output = new HashMap<>();
				
		for (Document matched: this.targetCorpus.documents) {
			if (!output.containsKey(matched)) {
				K sim1 = similarity.apply(doc.getString(), matched.getString());
				output.put(matched, sim1);
			}
		}
		
		return output.entrySet()
         .stream()
         .sorted(Map.Entry.comparingByValue());
	}
	
	static interface Normaliser extends Function<String,String> {}
	static interface Tokeniser extends Function<String,Stream<String>> {}

	public String summaryStats() {
		return new StringBuilder("Sources: ")
				.append("{"+sourceCorpus.summaryStats()+"}")
				.append(",\nTargets: ")
				.append("{"+targetCorpus.summaryStats()+"}").toString();
		
	}
	
	
}
