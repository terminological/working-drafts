package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.similarity.SimilarityScore;

import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.datatypes.Triple;

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
	
	public StringCrossMapper() {
		this(Collections.emptyList());
	}
	
	public StringCrossMapper(List<String> stopWords) {
		this(
			string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^a-zA-Z0-9\\s]", "-").replaceAll("\\s+", " ").toLowerCase(),
			string -> Stream.of(string.split("\\s+")).filter(s -> !s.equals("-")),
			stopWords
		);
	}
	
	public StringCrossMapper(Normaliser normaliser, Tokeniser tokeniser, List<String> stopWords) {
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
		Iterator<Weighted<Term>> it = doc.termsByTfIdf().iterator();
		
		Collection<Document> matching = targetCorpus.getDocuments();
		
		double score = 0;
		while (it.hasNext() && matching.size() > 1) {
			Weighted<Term> next = it.next();
			Term nextTerm = next.getTarget();
			Optional<Term> outputTerm = targetCorpus.getMatchingTerm(nextTerm);
			if (outputTerm.isPresent()) {
				Set<Document> tmp = new HashSet<>(outputTerm.get().getDocumentsUsing());
				tmp.retainAll(matching); 
				if (tmp.size() > 0) {
					matching = tmp;
					score += next.getWeight();
				}
			}
		}
		
		if (matching.size() == 1) return matching.stream().findFirst();
		if (score == 0) return Optional.empty();
		Document bestMatch = null;
		
		Double bestScore = Double.NEGATIVE_INFINITY;
		for (Document match: matching) {
			
			Stream<Weighted<Term>> found = match.termsByTfIdf();
			Stream<Weighted<Term>> orig = doc.termsByTfIdf();
			
			Map<Term,Double> tmp = new HashMap<>();
			
			found.forEach(wt -> tmp.put(wt.getTarget(), -wt.getWeight()));
			orig.forEach(wt -> {
				tmp.merge(
						wt.getTarget(), 
						-wt.getWeight(), 
						(w1,w2) -> -1*(w1+w2) //if terms match then add tfidfs and change sign 
						);
			});
			
			//essentially I want to do a cross join on Terms here and combine weights
			
			Double tmpScore = tmp.values().stream().mapToDouble(d->d).sum();
			
			if (tmpScore > bestScore) {
				bestScore = tmpScore;
				bestMatch = match;
			}
		}
		
		return Optional.ofNullable(bestMatch);
		
	}
	
	/**
	 * A score between 0 and 1 where 1 is no difference and 0.5 is the median similarity score in the corpus
	 * @param minValue
	 * @return
	 */
	public List<Triple<Document,Document,Double>> getAllMatchesBySimilarity(Double minValue, 
			Function<Document,Stream<Weighted<Term>>> mapper, BiFunction<Stream<Weighted<Term>>,Stream<Weighted<Term>>,Double> algorithm) {
		//<Double> scores = new ArrayList<>(); 
		List<Triple<Document,Document,Double>> match = new ArrayList<>();
		Double max = 0D;
		// int count = 0;
 		
 		for (Document doc: sourceCorpus.getDocuments()) {
 			Set<Document> targets = doc.getTerms().stream().flatMap(term -> targetCorpus.getMatchingTerm(term).stream()).flatMap(term -> term.getDocumentsUsing().stream()).collect(Collectors.toSet());
 			for (Document target: targets) {
 				Double distance = algorithm.apply(mapper.apply(doc),mapper.apply(target));
 				// scores.add(distance);
 				if (max < distance) max = distance; 
 				// count += 1;
 				// add to or create the nested map
 				match.add(Triple.create(doc, target, distance));
 			}
 		}
 		
 		/*Double median;
 		Collections.sort(scores);
 		if (scores.size() == 1) {
 			median = scores.get(0);
 		} else if (scores.size() % 2 == 1) {
 			median = scores.get(scores.size()/2);
 		} else {
 			median = (scores.get(scores.size()/2)+scores.get(scores.size()/2-1))/2;
 		}
 		// Use mean if median is zero
 		if (median == 0) median = total/scores.size();
 		// Double IQR = scores.get(scores.size()*3/4)-scores.get(scores.size()/4);
 		
 		
 		
 		// Use a logistic sigmoid function of the form f(x) = 1/(1+exp(-k*(x-x0))) where x0 is the median and k is chosen to approximate the IQR
 		// i.e. f(x0+IQR/2) = 0.75 from which we get k= -2*ln(1/3)/IQR
 		// Double x0 = median;
 		// Double k = -(2 * Math.log(1/3))/IQR;
 		// Function<Double,Double> normaliser = x -> 1/(1+Math.exp(-k*(x-x0)));
 		
 		// Function<Double,Double> normaliser = x -> median/(median+x);
 		for (Map<Document,Double> values: match.values()) {
 			for (Document key : values.keySet()) {
 				if (median == 0 && values.get(key) == 0) values.put(key, 1D);
 				else values.put(key, (median / (median+values.get(key))));
 			}
 		}*/
 		List<Triple<Document,Document,Double>> out = new ArrayList<>();
 		Iterator<Triple<Document,Document,Double>> it = match.iterator();
 		while (it.hasNext()) {
 			Triple<Document,Document,Double> next = it.next();
 			Double norm = 1-next.getThird()/max;
 			if (norm>minValue) out.add(Triple.create(next.getFirst(), next.getSecond(), norm));
 		}
 		
 		return out;
	}
	
	
	
	/**
	 * Computes the similarity for all combinations of source and target base on the raw text of the document, excluding normalisations
	 * @param minValue
	 * @param metric a class from org.apache.commons.text.similarity
	 * @return
	 */
	public <K extends Comparable<K>> Map<Document,Map<Document,K>> getAllMatchesByDistance(K maxValue, SimilarityScore<K> metric) {
		Map<Document,Map<Document,K>> match = new HashMap<>();
			
 		for (Document doc: sources.values()) {
 			Set<Document> targets = doc.getTerms().stream().flatMap(term -> targetCorpus.getMatchingTerm(term).stream()).flatMap(term -> term.getDocumentsUsing().stream()).collect(Collectors.toSet());
 			for (Document target: targets) {
 				K distance = getMatchByDistance(doc, target, metric); 
 				if (distance.compareTo(maxValue) < 0) {
 					Optional.ofNullable(match.get(doc)).ifPresentOrElse(
 	 						submap -> submap.put(target, distance),
 	 						() -> match.put(doc, FluentMap.with(target, distance)));
 				}
 			}
		}
 		
 		return match;
	}
	
	/*
	 * Takes an input document and calculates a similarity score for all the target documents based on the raw text of the document.
	 */
	private <K extends Comparable<K>> K getMatchByDistance(Document doc,Document target, SimilarityScore<K> similarity) {
		return similarity.apply(doc.getNormalised(), target.getNormalised());
	}
	
	public String summaryStats() {
		return new StringBuilder("Sources: ")
				.append("{"+sourceCorpus.summaryStats()+"}")
				.append(", Targets: ")
				.append("{"+targetCorpus.summaryStats()+"}").toString();
		
	}
	
	
}
