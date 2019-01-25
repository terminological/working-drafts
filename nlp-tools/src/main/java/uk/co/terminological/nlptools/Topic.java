package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.stream.Stream;

public class Topic {

	private int topicId;
	private SortedSet<Weighted<Term>> terms = new TreeSet<>();
	private SortedSet<Weighted<Document>> documents = new TreeSet<>();

	public Topic(int topic) {
		this.topicId = topic;
	}

	public void addTerm(Weighted<Term> wt) {
		terms.add(wt);
	}
	
	public Stream<Weighted<Term>> streamTerms() {
		return terms.stream();
	}
	
	public Stream<Weighted<Document>> streamDocuments() {
		return documents.stream();
	}
	
	public int getTopicId() {return topicId;}

	public void addDocument(Weighted<Document> create) {
		documents.add(create);
		create.getTarget().addTopic(Weighted.create(this, create.getWeight()));
	}
}
