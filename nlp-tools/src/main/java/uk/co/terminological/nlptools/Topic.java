package uk.co.terminological.nlptools;

import java.util.List;
import java.util.stream.Stream;

public class Topic {

	private int topicId;
	private List<Weighted<Term>> terms;

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
}
