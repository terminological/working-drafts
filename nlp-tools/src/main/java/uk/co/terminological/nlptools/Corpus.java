package uk.co.terminological.nlptools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.nlptools.StringCrossMapper.Normaliser;
import uk.co.terminological.nlptools.StringCrossMapper.Tokeniser;

/**
 * 
 * @author robchallen
 *
 */
public class Corpus {
	
	Map<String,Term> terms = new HashMap<>();
	Set<Document> documents = new HashSet<>();
	Normaliser normaliser;
	Tokeniser tokeniser;
	Set<String> stopWords;
	
	public Corpus(Normaliser normaliser, Tokeniser tokeniser, String[] stopWords) {
		this.normaliser = normaliser;
		this.tokeniser = tokeniser;
		this.stopWords = Stream.of(stopWords).map(normaliser).flatMap(tokeniser)
				.collect(Collectors.toSet());
	}

	// ============ BEAN METHODS =====================
	
	public void addDocument(Document doc) {
		this.documents.add(doc);
	}
	
	public Stream<Term> streamTerms() {
		return terms.values().stream();
	}
	
	public Stream<Document> streamDocuments() {
		return documents.stream();
	}
	
	// ============ SPECIFIC METHODS =====================
	
	
	
	
	/**
	 * Get a term if present in this corpus
	 * @param nextTerm
	 * @return
	 */
	public Optional<Term> getMatchingTerm(Term nextTerm) {
		return Optional.ofNullable(terms.get(nextTerm.tag));
	}

	/**
	 * create a new term or fetch an existing one from the corpus. 
	 * @param tag
	 * @return
	 */
	public Term getTermFrom(String tag) {
		if (!terms.containsKey(tag)) {
			terms.put(tag, new Term(tag, this));
		}
		return terms.get(tag);
	}
	
	public int countCorpusDocuments() {
		return documents.size();
	}
	
	public int countCorpusTerms() {
		return documents.stream().collect(Collectors.summingInt(d -> d.countTermsInDocument()));
	}
	
	public int countUniqueTerms() {
		return terms.size();
	}
	
	/**
	 * return counts of documents and unique terms in this corpus
	 * @return
	 */
	public String summaryStats() {
		return new StringBuilder()
				.append("Documents: "+countCorpusDocuments()+", ")
				.append("Terms: "+countCorpusTerms()+", ")
				.append("Unique terms: "+countUniqueTerms()).toString();
	}

	public Set<Document> getDocuments() {
		return this.documents;
	}
	
}