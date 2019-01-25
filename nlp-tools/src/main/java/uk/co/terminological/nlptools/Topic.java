package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;
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
	
	public Stream<Weighted<Term>> getTerms() {
		return terms.stream();
	}
	
	public int getTopicId() {return topicId;}

	public void addDocument(Weighted<Document> create) {
		documents.add(create);
		create.getTarget().addTopic(Weighted.create(this, create.getWeight()));
	}
}
