package uk.co.terminological.literaturereview;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
	Corpus sourceComponents;
	Corpus targetComponents;
	Normaliser normaliser;
	Tokeniser tokeniser;
	
	public void addSource(String id, String source) {
		this.sources.put(id, new Document(id, source, sourceComponents));
	}
	
	public Corpus getSource() {return sourceComponents;}
	public Corpus getTarget() {return targetComponents;}
	
	public void addTarget(String id, String target) {
		this.targets.put(id, new Document(id, target, targetComponents));
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
		sourceComponents = new Corpus(normaliser, tokeniser, stopWords);
		targetComponents = new Corpus(normaliser, tokeniser, stopWords);
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
		ArrayList<Term> orderedTerms = new ArrayList<>(doc.components);
		orderedTerms.sort(new Comparator<Term>() {
			@Override
			public int compare(Term t1, Term t2) {
				return doc.termSignificance(t2).compareTo(doc.termSignificance(t1));
			}
		});
		Iterator<Term> it = orderedTerms.iterator();
		
		Set<Document> matching = new HashSet<>(targets.values());
		
		double similarity=0D;
		int i = 0;
		
		while (it.hasNext() && matching.size() != 1) {
			Term nextTerm = it.next();
			Term outputTerm = targetComponents.termFrom(nextTerm.tag);
			Set<Document> tmp = outputTerm.norms.keySet();
			tmp.retainAll(matching); 
			if (tmp.size() > 0) {
				matching = tmp;
				similarity += doc.termSignificance(nextTerm);
				i++;
			} else {
				break;
			}
		}
		
		if (i<2) return Optional.empty();
		final double sim = similarity;
		return matching.stream().map(d -> (Entry<Document,Double>) new SimpleEntry<Document,Double>(d,sim)).findAny();
		
	}
	
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
	
	private Stream<Entry<Document,Double>> getAllMatchesBySignificance(Document doc) {
		
		Iterator<Term> it = doc.components.iterator();
		Map<Document,Double> output = new HashMap<>();
		
		while (it.hasNext()) {
			Term nextTerm = it.next();
			Term outputTerm = targetComponents.termFrom(nextTerm.tag);
			Set<Document> tmp = outputTerm.norms.keySet();
			for (Document matched: tmp) {
				Double soFar = 1D;
				if (output.containsKey(matched)) {
					soFar = output.get(matched);
				}
				output.put(matched, soFar*doc.termSignificance(nextTerm)*matched.termSignificance(outputTerm));
			}
		}
		
		return output.entrySet()
         .stream()
         .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()));
	}
	
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
	
	private <K extends Comparable<K>> Stream<Entry<Document,K>> getAllMatchesByDistance(Document doc, SimilarityScore<K> similarity) {
		
		Iterator<Term> it = doc.components.iterator();
		Map<Document,K> output = new HashMap<>();
		String docNorm = Document.termsToString(doc.tfidfOrder()," ");//).normalisedOrder()," ");
		
		
		while (it.hasNext()) {
			Term nextTerm = it.next();
			Term outputTerm = targetComponents.termFrom(nextTerm.tag);
			Set<Document> tmp = outputTerm.norms.keySet();
			for (Document matched: tmp) {
				if (!output.containsKey(matched)) {
					String matchedNorm = Document.termsToString(matched.tfidfOrder()," ");//.normalisedOrder()," ");
					K sim1 = similarity.apply(docNorm, matchedNorm);
					output.put(matched, sim1);
				}
			}
		}
		
		return output.entrySet()
         .stream()
         .sorted(Map.Entry.comparingByValue());
	}
	
	/**************************************************
	 * 
	 * @author robchallen
	 *
	 */
	public static class Corpus {
		Map<String,Term> terms = new HashMap<>();
		List<Document> documents = new ArrayList<>();
		Normaliser normaliser;
		Tokeniser tokeniser;
		Set<String> stopWords;
		
		public Corpus(Normaliser normaliser, Tokeniser tokeniser, String[] stopWords) {
			this.normaliser = normaliser;
			this.tokeniser = tokeniser;
			this.stopWords = Stream.of(stopWords).map(normaliser).flatMap(tokeniser)
					.collect(Collectors.toSet());
		}

		public Term termFrom(String tag) {
			if (!terms.containsKey(tag)) {
				terms.put(tag, new Term(tag, this));
			}
			return terms.get(tag);
		}
		
		public void addDocument(Document doc) {
			this.documents.add(doc);
		}
		public int corpusDocuments() {
			return documents.size();
		}
		
		public Stream<Term> streamTerms() {
			return terms.values().stream();
		}
		
		public Stream<Document> streamDocuments() {
			return documents.stream();
		}
		
	}
	
	public static class Term {
		String tag;
		int count;
		Corpus map;
		HashMap<Document,Integer> norms = new HashMap<>();
		
		public Term(String tag, Corpus map) {
			this.tag = tag;
			this.map = map;
		}
		
		public void add(Document norm) {
			if (norms.containsKey(norm)) {
				norms.put(norm, norms.get(norm)+1);
			} else {
				norms.put(norm,1);
			}
			count+=1;
		}
		
		public int hashCode() {return tag.hashCode();}
		public boolean equals(Object o) {
			if (o instanceof Term) {
				return ((Term) o).tag.equals(tag);
			} else return false;
		}
		public String toString() {return tag+" ["+idf()+":"+count+"]";} 
		
		public int documentsWithTermCount() {return count;}
		public Double idf() {
			//probabilistic idf
			return Math.log(((double) map.corpusDocuments()-this.documentsWithTermCount())/this.documentsWithTermCount());
		}
		
	}
	
	public static class Document {
		
		String identifier;
		String string;
		String normalised;
		List<Term> components = new ArrayList<>();
		Corpus corpus;
		
		Document(String id, String string, Corpus corpus) {
			this.identifier = id;
			this.corpus = corpus;
			this.string = string;
			this.normalised = corpus.normaliser.apply(string);
			corpus.tokeniser.apply(normalised)
			.filter(t-> !corpus.stopWords.contains(t))
			.forEach(tag ->
				{
					Term tmp = corpus.termFrom(tag);
					components.add(tmp);
					tmp.add(this);
				});
			this.corpus.addDocument(this);
		}
		
		public int hashCode() {return string.hashCode();}
		public boolean equals(Object o) {
			if (o instanceof Document) return ((Document) o).string.equals(string);
			else return false;
		}
		public String toString() {
			return string+" ("+
					components.stream()
						.map(t -> t.tag+" ["+termSignificance(t)+"]")
						.collect(Collectors.joining(","))+")";
		}
		
		public int occurencesInDocument(Term term) {
			return (int) components.stream().filter(t -> t.equals(term)).count();
		}
		public int termsInDocumentTotal() {return components.size();}
		
		public Double termSignificance(Term term) { 
			Double tf = ((double) occurencesInDocument(term));// / termsInDocumentTotal();
			if (term.tag.length() < 3) tf=tf/10;
			return tf*term.idf();
		}
		
		public List<Term> normalisedOrder() {
			List<Term> tmp = new ArrayList<>(components);
			tmp.sort((t1,t2) -> t1.tag.compareTo(t2.tag));
			return tmp; //.stream().map(t -> t.tag).collect(Collectors.joining(" "));
		}
		
		public List<Term> tfidfOrder() {
			ArrayList<Term> orderedTerms = new ArrayList<>(components);
			orderedTerms.sort(new Comparator<Term>() {
				@Override
				public int compare(Term t1, Term t2) {
					return termSignificance(t2).compareTo(termSignificance(t1));
				}
			});
			return orderedTerms;
		}
		
		public static String termsToString(List<Term> terms, String sep) {
			return terms.stream().map(t -> t.tag).collect(Collectors.joining(sep));
		}
	}
	
	static interface Normaliser extends Function<String,String> {}
	static interface Tokeniser extends Function<String,Stream<String>> {}
	
	
}
