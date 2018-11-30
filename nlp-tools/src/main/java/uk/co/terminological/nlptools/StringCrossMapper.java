package uk.co.terminological.nlptools;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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
			string -> Stream.of(string.split("\\S+")).filter(s -> !s.equals("-")),
			stopWords
		);
	}
	
	public StringCrossMapper(Normaliser normaliser, Tokeniser tokeniser, String[] stopWords) {
		this.normaliser = normaliser;
		this.tokeniser = tokeniser;
		sourceCorpus = new Corpus(normaliser, tokeniser, stopWords);
		targetCorpus = new Corpus(normaliser, tokeniser, stopWords);
	}
	
	
	public Map<Document,Entry<Document,Double>> getBestMatches() {
		Map<Document,Entry<Document,Double>> match = new HashMap<>();
 		for (Entry<String,Document> source: sources.entrySet()) {
			getBestMatch(source.getValue()).ifPresent(doc2 -> match.put(source.getValue(), new SimpleEntry<Document,Double>(doc2.getKey(), doc2.getValue()))); 
		}
 		return match;
	}
	
	private Optional<Entry<Document,Double>> getBestMatch(Document doc) {
		//if (targets.containsKey(doc.normalised)) return Optional.of(targets.get(doc.normalised));
		ArrayList<Term> orderedTerms = new ArrayList<>(doc.getComponents());
		orderedTerms.sort(doc.descendingTfIdf());
		Iterator<Term> it = orderedTerms.iterator();
		
		Set<Document> matching = new HashSet<>(targets.values());
		
		double similarity=0D;
		int i = 0;
		
		while (it.hasNext() && matching.size() != 1) {
			Term nextTerm = it.next();
			Term outputTerm = targetCorpus.getTermFrom(nextTerm.tag);
			Set<Document> tmp = outputTerm.getDocumentsUsing();
			tmp.retainAll(matching); 
			if (tmp.size() > 0) {
				matching = tmp;
				similarity += doc.tfIdf(nextTerm);
				i++;
			} else {
				break;
			}
		}
		
		if (i<2) return Optional.empty();
		final double sim = similarity;
		return matching.stream().map(d -> (Entry<Document,Double>) new SimpleEntry<Document,Double>(d,sim)).findAny();
		
	}
	
	/**
	 * 
	 * @param minValue
	 * @return
	 */
	public Map<Document,Map<Document,Double>> getAllMatchesBySignificance(Double minValue) {
		Map<Document,Map<Document,Double>> match = new HashMap<>();
 		for (Document doc: sources.values()) {
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
		}
 		return match;
	}
	
	/*
	 * Calculates a significance of similarity based on the tfidf
	 * For every term in the source document
	 * Find documents containing that term in the target corpus
	 * Calculate the contribution that term will have on an overall score metric and multiply that to the current score for every matching document
	 * Move onto next term.
	 * 
	 */
	private Stream<Entry<Document,Double>> getAllMatchesBySignificance(Document doc) {
		
		Iterator<Entry<Term,Double>> it = doc.tfIdfsDescending().entrySet().iterator();
		Map<Document,Double> output = new HashMap<>();
		
		while (it.hasNext()) {
			Entry<Term,Double> nextTfidf = it.next();
			Term nextTerm = nextTfidf.getKey();
			Double docTfidfScore = nextTfidf.getValue();
			Optional<Term> optOutputTerm = targetCorpus.getMatchingTerm(nextTerm);
			optOutputTerm.ifPresent(outputTerm -> {
				Set<Document> tmp = outputTerm.getDocumentsUsing();
				for (Document matched: tmp) {
					Double soFar = 1D;
					if (output.containsKey(matched)) {
						soFar = output.get(matched);
					}
					output.put(matched, soFar*docTfidfScore*matched.tfIdf(outputTerm));
				}
			});
		}
		
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
