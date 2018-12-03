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
	
	private Map<String,Term> terms = new HashMap<>();
	private Set<Document> documents = new HashSet<>();
	private Normaliser normaliser;
	private Tokeniser tokeniser;
	private Set<String> stopWords;
	private int termsInCorpus;
	
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
	
	public Set<Document> getDocuments() {
		return documents;
	}
	
	public Normaliser getNormaliser() {
		return normaliser;
	}

	public Tokeniser getTokeniser() {
		return tokeniser;
	}

	public Set<String> getStopWords() {
		return stopWords;
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
		Term tmp = terms.get(tag);
		tmp.incrementUsed();
		termsInCorpus += 1;
		return tmp;
	}
	
	public int countCorpusDocuments() {
		return documents.size();
	}
	
	public int countCorpusTerms() {
		return termsInCorpus;
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

	
	
}