package uk.co.terminological.nlptools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.nlptools.StringCrossMapper.Document;
import uk.co.terminological.nlptools.StringCrossMapper.Normaliser;
import uk.co.terminological.nlptools.StringCrossMapper.Tokeniser;

/**
 * 
 * @author robchallen
 *
 */
public class Corpus {
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

	public Optional<Term> getMatchingTerm(Term nextTerm) {
		return Optional.ofNullable(terms.get(nextTerm.tag));
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
	
	public String summaryStats() {
		return new StringBuilder()
				.append("Documents: "+documents.size()+", ")
				.append("Unique terms: "+terms.size()).toString();
	}
	
}