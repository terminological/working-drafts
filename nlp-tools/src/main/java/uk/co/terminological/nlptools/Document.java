package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A document is an identifiable string associated with a corpus
 */
public class Document {
	
	private String identifier;
	private String string;
	private String normalised;
	private List<Term> terms = new ArrayList<>();
	private HashMap<Term,Integer> termCounts = new HashMap<>();
	private Corpus corpus;
	
	protected Document(String id, String string, Corpus corpus) {
		this.identifier = id;
		this.corpus = corpus;
		this.string = string;
		this.normalised = corpus.getNormaliser().apply(string);
		corpus.getTokeniser().apply(normalised)
			.filter(t-> !corpus.getStopWords().contains(t))
			.forEach(tag -> {
				//Create a new term
				Term tmp = corpus.createTermFrom(tag);
				terms.add(tmp);
				tmp.add(this);
				termCounts.put(tmp, termCounts.getOrDefault(tmp,0)+1);
			});
		this.corpus.addDocument(this);
	}
	
	// ============ BEAN METHODS =====================
	
	public String getIdentifier() {
		return identifier;
	}

	public String getString() {
		return string;
	}

	public String getNormalised() {
		return normalised;
	}

	public Set<Term> getTerms() {
		return termCounts.keySet();
	}

	public Corpus getCorpus() {
		return corpus;
	}
	
	public int hashCode() {
		return identifier.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o instanceof Document) return ((Document) o).identifier.equals(identifier);
		else return false;
	}
	
	public String toString() {
		return string+" ("+
				terms.stream()
					.map(t -> t.tag+"  {tfidf:"+tfIdf(t)+",entropy: "+t.shannonEntropy()+"}")
					.collect(Collectors.joining(","))+")";
	}
	
	// ============ SPECIFIC METHODS =====================
	
	public int countOccurencesInDocument(Term term) {
		return termCounts.get(term);
	}
	
	public int countTermsInDocument() {
		return terms.size();
	}
	
	/**
	 * Calculate the tfidf fromt the frequency of terms in this document / the frequency of documents containing this term in the corpus
	 * <br/>
	 * In the case of the term frequency tf(t,d), the simplest choice is to use the raw count of a term in a document, i.e., the number of times that term t occurs in document d.<br\> 
	 * If we denote the raw count by ft,d, then the simplest tf scheme is tf(t,d) = ft,d. <br/>
	 * Other possibilities include:<br/>
	 * Boolean "frequencies": tf(t,d) = 1 if t occurs in d and 0 otherwise;<br/>
	 * term frequency adjusted for document length : ft,d ÷ (number of words in d)<br/>
	 * logarithmically scaled frequency: tf(t,d) = log (1 + ft,d)<br/>
	 * augmented frequency, to prevent a bias towards longer documents, <br/>
	 * e.g. raw frequency divided by the raw frequency of the most occurring term in the document:
	 * @param term
	 * @return
	 */
	public Double tfIdf(Term term) { 
		Double tf = ((double) countOccurencesInDocument(term));// / termsInDocumentTotal();
		if (term.tag.length() < 3) tf=tf/10;
		return tf*term.idf();
	}
	
	
	public Double shannonEntropy() {
		return terms.stream().collect(Collectors.summingDouble(t -> t.shannonEntropy()));
	}
	
	/**
	 * returns the terms of the document in entropy descending order.
	 */
	public List<Term> entropyOrder() {
		List<Term> tmp = new ArrayList<>(terms);
		tmp.sort((t1,t2) -> t2.shannonEntropy().compareTo(t1.shannonEntropy()));
		return tmp;
	}
	
	/**
	 * lists the terms in the document according to descending tfidf score.
	 * @return
	 */
	public List<Term> tfIdfOrder() {
		List<Term> orderedTerms = new ArrayList<>(terms);
		orderedTerms.sort((t1,t2) -> tfIdf(t2).compareTo(tfIdf(t1)));
		return orderedTerms;
	}
	
	
	public Map<Term,Double> termsByTfIdf() {
		LinkedHashMap<Term,Double> out = new LinkedHashMap<>();
		HashMap<Term,Double> tmp = new LinkedHashMap<>();
		termCounts.keySet().forEach(c -> tmp.put(c, tfIdf(c)));
		tmp.entrySet().stream()
	    	.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	    	.forEach(kv -> out.put(kv.getKey(), kv.getValue()));
		return out;
	}
	
	public Map<Term,Double> termsByEntropy() {
		LinkedHashMap<Term,Double> out = new LinkedHashMap<>();
		HashMap<Term,Double> tmp = new LinkedHashMap<>();
		termCounts.keySet().forEach(c -> tmp.put(c, c.shannonEntropy()));
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
	@Deprecated
	public static String termsToString(List<Term> terms, String sep) {
		return terms.stream().map(t -> t.tag).collect(Collectors.joining(sep));
	}
	
	public Comparator<? super Term> descendingTfIdf() {return new Comparator<Term>() {
		@Override
		public int compare(Term t1, Term t2) {
			return Document.this.tfIdf(t2).compareTo(Document.this.tfIdf(t1));
		}
	};}

	
}