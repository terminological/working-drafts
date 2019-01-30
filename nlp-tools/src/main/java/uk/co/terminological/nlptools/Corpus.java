package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cc.mallet.types.Alphabet;
import cc.mallet.types.Instance;
import uk.co.terminological.datatypes.Tuple;

/**
 * 
 * @author robchallen
 *
 */
public class Corpus {
	
	private Map<String,Term> terms = new HashMap<>();
	private Map<String,Document> documents = new HashMap<>();
	private Normaliser normaliser;
	private Tokeniser tokeniser;
	private List<Predicate<String>> filters = new ArrayList<>();
	private int termsInCorpus;
	private Map<Integer,Topic> topics = new HashMap<>();
	//private Map<Term,Integer> termCounts = new HashMap<>();
	
	//TODO: create a subset of this corpus
	//TODO: comparative analysis of different corpus - will need some form of Term mapping
	
	
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
	
	public Corpus withTokenFilter(Predicate<String> filter) {
		this.filters.add(filter);
		return this;
	}
	
	public Corpus withStopwordFilter(List<String> stopwords) {
		this.filters.add(Filters.stopwords(stopwords, normaliser, tokeniser));
		return this;
	}

	// ============ BEAN METHODS =====================
	
	public Document addDocument(Document doc) {
		this.documents.put(doc.getIdentifier(),doc);
		return doc;
	}
	
	public Document addDocument(String id, String source) {
		return addDocument(new Document(id, source, this));
	}
	
	public Document addDocument(String source) {
		return addDocument(new Document(UUID.randomUUID().toString(), source, this));
	}
	
	public Stream<Term> streamTerms() {
		return terms.values().stream();
	}
	
	public boolean hasTopics() {
		return topics.size() !=0;
	}
	
	public Stream<Topic> streamTopics() {
		return topics.values().stream();
	}
	
	public Collection<Document> getDocuments() {
		return documents.values();
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
	
	public Optional<Document> getById(String id) {
		return Optional.ofNullable(this.documents.getOrDefault(id,null));
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
		Term tmp = terms.getOrDefault(tag, new Term(tag, this, terms.size()));
		terms.put(tag, tmp);
		// termCounts.put(tmp, termCounts.getOrDefault(tmp,0)+1);
		termsInCorpus += 1;
		return tmp;
	}
	
	public int countCorpusDocuments() {
		return documents.size();
	}
	
	public int countCorpusCollocations(int spanLength) {
		return documents.values().stream().map(d -> d.countCollocations(spanLength)).mapToInt(i -> i).sum();
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
	
	public Stream<Weighted<Term>> getTermsByTotalEntropy() {
		SortedSet<Weighted<Term>> out = Weighted.descending();
		terms.values().forEach(t -> out.add(Weighted.create(t, totalShannonEntropy(t))));
		return out.stream();
	}
	
	/**
	 * This is an aggregated unnormalised mutual information score for each combination of terms
	 * in the corpus presented in descending order. Missing values are zero.
	 * @return
	 */
	public Stream<Weighted<Map.Entry<Term,Term>>> getMutualInformation() {
		SortedSet<Weighted<Map.Entry<Term,Term>>> out = Weighted.descending();
		this.terms.values().forEach(source -> {
			source.mutualInformation().forEach(tv -> out.add(Weighted.create(Tuple.create(source, tv.getTarget()),  tv.getWeight())));
		});
		return out.stream();
	}	
	
	/**
	 * This is an aggregated normalised mutual information score for each combination of terms
	 * in the corpus. it is presented in term order.
	 * @return
	 */
	public Stream<Weighted<Map.Entry<Term,Term>>> getNormalisedMutualInformation() {
		List<Weighted<Map.Entry<Term,Term>>> out = new ArrayList<>();
		//EavMap<Term,Term,Double> out = new EavMap<Term,Term,Double>();
		HashSet<Term> targets = new HashSet<Term>(this.terms.values());
		this.terms.values().forEach(source -> {
			Map<Term,Double> normMi = new HashMap<>();
			source.mutualInformation().forEach(wt -> normMi.put(wt.getTarget(), wt.getWeight()));
			source.cooccurenceProbablities().forEach(wt -> {
				normMi.merge(wt.getTarget(), wt.getWeight(), (mi,prob) -> -mi / Math.log(prob)); //TODO: this only workd because the mI and cooccurence are the same length.
			});
			targets.remove(source);
			targets.forEach(target -> {
				normMi.merge(target, -1D, (a,b) -> a); //fills in missing values with -1
			});
			normMi.forEach((target,weight) ->
				out.add(Weighted.create(Tuple.create(source, target),  weight)));
		});
		return out.stream();
	}
	
	public Stream<Weighted<Map.Entry<Term,Term>>> getCollocations(int span) {
		SortedSet<Weighted<Map.Entry<Term,Term>>> out = Weighted.descending();
		this.terms.values().forEach(source -> {
			source.chiSqCollocations(span).forEach((wt) -> {
				out.add(Weighted.create(Tuple.create(source,wt.getTarget()), wt.getWeight()));
			});
		});
		return out.stream();
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
		return documents.values().stream().map(d -> d.countCollocations(spanLength, sizeCollocation)).mapToInt(i -> i).sum();
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
	
	protected Iterator<Instance> tokenSequenceIterator() {
		Iterator<Document> docIt = this.documents.values().iterator();
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
						doc.getFeatures(),
						doc.getIdentifier(),
						doc.getString()
				);
			}
		};
	}

	Alphabet keys = new Alphabet();
	
	protected Alphabet getAlphabet() {
		return keys;
	}

	protected Topic addTopic(int topic) {
		if (!this.topics.containsKey(topic)) {
			Topic tmp = new Topic(topic);
			this.topics.put(topic, tmp);
		}
		return this.topics.get(topic);
	}
	
	
}