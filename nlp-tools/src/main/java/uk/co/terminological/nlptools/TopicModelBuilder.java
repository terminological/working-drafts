package uk.co.terminological.nlptools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;

public class TopicModelBuilder {

	public static TopicModelBuilder create(Corpus corpus) {
		return new TopicModelBuilder(corpus);
	}

	private Corpus corpus;
	private ArrayList<Pipe> pipelist;
	private InstanceList instances;
	private int topics = 5;
	
	private TopicModelBuilder(Corpus corpus) {
		
		this.corpus = corpus;
		this.pipelist = new ArrayList<>();
		this.pipelist.add( new TokenSequence2FeatureSequence() );
		this.instances = new InstanceList(new SerialPipes(pipelist));
	}
	
	public TopicModelBuilder withTopics(int topics) {
		this.topics = topics;
		return this;
	}
	
	public Result execute(double alpha, double beta) throws IOException {
		this.instances.addThruPipe(corpus.tokenSequenceIterator());
		ParallelTopicModel model = new ParallelTopicModel(topics, alpha*topics, beta);
		model.addInstances(instances);
		model.setNumThreads(2);
		model.setNumIterations(1000);
        model.estimate();
        return new Result(model,instances);
	}
	
	public static class Result {
		
		public Result(ParallelTopicModel model, InstanceList instances) {
			super();
			this.model = model;
			this.instances = instances;
			this.dataAlphabet = instances.getDataAlphabet();
		}
		
		ParallelTopicModel model;
		InstanceList instances;
		Alphabet dataAlphabet;
		
		public void printTopics() {
			PrintWriter pw = new PrintWriter(System.out);
			model.topicPhraseXMLReport(pw, 30);
			pw.flush();
			pw.close();
		}
	}
	
	
}
