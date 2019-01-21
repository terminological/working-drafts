package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.EavMap;
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
	private List<Predicate<String>> filters = new ArrayList<>();
	private int termsInCorpus;
	private Map<Term,Integer> termCounts = new HashMap<>();
	
	@SafeVarargs
	public Corpus(Normaliser normaliser, Tokeniser tokeniser, String[] stopWords, Predicate<String>... otherFilters) {
		this.normaliser = normaliser;
		this.tokeniser = tokeniser;
		Set<String> stopWordList = Stream.of(stopWords).map(normaliser).flatMap(tokeniser)
				.collect(Collectors.toSet());
		this.filters.add(t -> !stopWordList.contains(t));
		this.filters.addAll(Arrays.asList(otherFilters));
	}
	
	public static Corpus create() {
		return new Corpus(
			string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^a-zA-Z0-9\\s]", "-").replaceAll("\\s+", " ").toLowerCase(),
			string -> Stream.of(string.split("\\s+")).filter(s -> !s.equals("-")),
			new String[] {}
		);
	}

	// ============ BEAN METHODS =====================
	
	public void addDocument(Document doc) {
		this.documents.add(doc);
	}
	
	public void addDocument(String id, String source) {
		addDocument(new Document(id, source, this));
	}
	
	public void addDocument(String source) {
		addDocument(new Document(UUID.randomUUID().toString(), source, this));
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

	public List<Predicate<String>> getFilters() {
		return filters;
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
	
	public EavMap<Term,Term,Double> getMutualInformation() {
		EavMap<Term,Term,Double> out = new EavMap<Term,Term,Double>();
		HashSet<Term> targets = new HashSet<Term>(this.terms.values());
		this.terms.values().forEach(source -> {
			Map<Term,Double> probs = source.cooccurenceProbablity();
			Map<Term,Double> mis = source.mutualInformation();
			targets.remove(source);
			targets.forEach(target -> {
				Double prob = probs.getOrDefault(target, 0D);
				Double mi = mis.getOrDefault(target, Double.NEGATIVE_INFINITY);
				out.add(source, target,  -mi / Math.log(prob));
			});
		});
		return out;
	}
}