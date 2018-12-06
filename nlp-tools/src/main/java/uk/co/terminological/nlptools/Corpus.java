package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private Map<Term,Integer> termCounts = new HashMap<>();
	
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
	
	// TODO: https://en.wikipedia.org/wiki/Okapi_BM25
	
	
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
	protected Term createTermFrom(String tag) {
		Term tmp = terms.getOrDefault(tag, new Term(tag, this));
		terms.put(tag, tmp);
		termCounts.put(tmp, termCounts.getOrDefault(tmp,0)+1);
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

	public Map<Term,Integer> getTermCounts() {
		return termCounts;
	}
	
	public int countTermsUsage(Term term) {
		return termCounts.getOrDefault(term, 0);
	}
	
	public Double totalShannonEntropy(Term term) {
		return term.shannonEntropy()*countTermsUsage(term);
	}
	
	public List<Term> getTermsByTotalEntropy() {
		List<Term> out = new ArrayList<>(terms.values());
		out.sort((t1,t2) -> totalShannonEntropy(t2).compareTo(totalShannonEntropy(t2)));
		return out;
	}
}