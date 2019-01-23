package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cc.mallet.types.Instance;
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
	//private Map<Term,Integer> termCounts = new HashMap<>();
	
	
	//TODO: Generate better 
	@SafeVarargs
	public Corpus(Normaliser normaliser, Tokeniser tokeniser, List<String> stopwords, Predicate<String>... otherFilters) {
		this.normaliser = normaliser;
		this.tokeniser = tokeniser;
		this.filters.add(Filters.stopwords(stopwords, normaliser, tokeniser));
		this.filters.addAll(Arrays.asList(otherFilters));
	}
	
	public static Corpus create() {
		return new Corpus(
			string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^a-zA-Z0-9\\s]", "-").replaceAll("\\s+", " ").toLowerCase(),
			string -> Stream.of(string.split("\\s+")).filter(s -> !s.equals("-")),
			Collections.emptyList()
		);
	}

	// ============ BEAN METHODS =====================
	
	public void addDocument(Document doc) {
		this.documents.add(doc);
	}
	
	public void addDocument(String id, String name, String source) {
		addDocument(new Document(id, name, source, this));
	}
	
	public void addDocument(String name, String source) {
		addDocument(new Document(UUID.randomUUID().toString(), name, source, this));
	}
	
	public void addDocument(String source) {
		addDocument(new Document(UUID.randomUUID().toString(), source, source, this));
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
		// termCounts.put(tmp, termCounts.getOrDefault(tmp,0)+1);
		termsInCorpus += 1;
		return tmp;
	}
	
	public int countCorpusDocuments() {
		return documents.size();
	}
	
	public int countCorpusCollocations(int spanLength) {
		return documents.stream().map(d -> d.countCollocations(spanLength)).mapToInt(i -> i).sum();
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

	/*public Map<Term,Integer> getTermCounts() {
		return termCounts;
	}
	
	public int countTermsUsage(Term term) {
		return termCounts.getOrDefault(term, 0);
	}*/
	
	public Double totalShannonEntropy(Term term) {
		return term.shannonEntropy()*term.countOccurrences();
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
			Map<Term,Double> probs = source.cooccurenceProbablities();
			Map<Term,Double> mis = source.mutualInformation();
			targets.remove(source);
			targets.forEach(target -> {
				if (probs.containsKey(target) && mis.containsKey(target)) {
					Double prob = probs.get(target);
					Double mi = mis.get(target);
					out.add(source, target,  -mi / Math.log(prob)); }
				else {
					out.add(source, target,  -1D);
				}
			});
		});
		return out;
	}
	
	public EavMap<Term,Term,Double> getCollocations(int span) {
		EavMap<Term,Term,Double> out = new EavMap<Term,Term,Double>();
		this.terms.values().forEach(source -> {
			Map<Term,Double> chiSq = source.chiSqCollocations(span);
			out.add(source, chiSq);
		});
		return out;
	}
	
	public int countCollocation(Set<Term> terms, int span) {
		if (terms.isEmpty()) return 0;
		Term start = terms.iterator().next();
		terms.remove(start);
		return (int) start.getInstances().stream().filter(ti -> {
			Set<Term> neighbours = ti.getNeighbours(span)
				.stream()
				.map(n -> n.getTerm())
				.collect(Collectors.toSet());
			neighbours.retainAll(terms);
			return neighbours.size() < terms.size();
		}).count();
	}
	
	public int countCorpusCollocations(int spanLength, int sizeCollocation) {
		return documents.stream().map(d -> d.countCollocations(spanLength, sizeCollocation)).mapToInt(i -> i).sum();
	}
	
	public double tStatisticCollocation(Set<Term> terms, int span) {
		int N = countCorpusCollocations(span, terms.size());
		double p = countCollocation(terms,span) / N;
		double p0 = terms.stream().map(t -> t.probabilityOccurrence()).reduce((d1,d2) -> d1*d2).orElse(0D);
		double estVar = p0*(1-p0);
		return Math.sqrt(Math.pow((p - p0),2)/estVar);
	}
	
	public int countCooccurrence(Set<Term> terms) {
		Set<Document> out = new HashSet<>();
		for (Term t: terms) {
			if (out.isEmpty()) out.addAll(t.getDocumentsUsing());
			else out.retainAll(t.getDocumentsUsing());
			if (out.isEmpty()) return 0; 
		}
		return out.size();
	}
	
	public Iterator<Instance> tokenSequenceIterator() {
		Iterator<Document> docIt = this.documents.iterator();
		return new Iterator<Instance>() {

			@Override
			public boolean hasNext() {
				return docIt.hasNext();
			}

			@Override
			public Instance next() {
				Document doc = docIt.next();
				return new Instance(
						doc.asTokenSequence(),
						doc.getIdentifier(),
						doc.getName(),
						doc.getString()
				);
			}
		};
	}
	
	
}