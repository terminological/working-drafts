package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Document {
	
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
	
	public int hashCode() {return identifier.hashCode();}
	public boolean equals(Object o) {
		if (o instanceof Document) return ((Document) o).identifier.equals(identifier);
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
	
	/**
	 * lists the terms in the document according to descending tfidf score.
	 * @return
	 */
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
	
	
	public Map<Term,Double> tfIdfsDescending() {
		LinkedHashMap<Term,Double> out = new LinkedHashMap<>();
		HashMap<Term,Double> tmp = new LinkedHashMap<>();
		components.forEach(c -> {
			if (!tmp.containsKey(c)) tmp.put(c, termSignificance(c));
		});
		tmp.entrySet().stream()
	    	.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	    	.forEach(kv -> out.put(kv.getKey(), kv.getValue()));
		return out;
	}
	
	/**
	 * reconstructs a document from a list of terms and a separator
	 * @param terms
	 * @param sep
	 * @return A string of the document
	 */
	public static String termsToString(List<Term> terms, String sep) {
		return terms.stream().map(t -> t.tag).collect(Collectors.joining(sep));
	}
}