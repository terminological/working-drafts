package uk.co.terminological.nlptools;

public class TopicModelBuilder {

	public static TopicModelBuilder create(Corpus corpus) {
		return new TopicModelBuilder(corpus);
	}

	private Corpus corpus;
	
	private  TopicModelBuilder(Corpus corpus) {
		this.corpus = corpus;
	}
	
	
}
