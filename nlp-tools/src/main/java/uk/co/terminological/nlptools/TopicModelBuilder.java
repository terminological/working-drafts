package uk.co.terminological.nlptools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.DMRTopicModel;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicModelDiagnostics;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class TopicModelBuilder implements Serializable {

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

	public Result execute(double alpha, double beta) {
		try {
			this.instances.addThruPipe(corpus.tokenSequenceIterator());
			ParallelTopicModel model = new ParallelTopicModel(topics, alpha*topics, beta);
			model.addInstances(instances);
			model.setNumThreads(2);
			model.setNumIterations(1000);
			model.estimate();
			return new Result(model,this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Result executeDMR() {
		try {
		this.instances.addThruPipe(corpus.tokenSequenceIterator());
		DMRTopicModel model = new DMRTopicModel(topics);
		model.addInstances(instances);
		model.setNumThreads(2);
		model.setNumIterations(1000);
		model.estimate();
		return new Result(model,this);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static class Result implements Serializable {

		public Result(ParallelTopicModel model, TopicModelBuilder builder) {
			super();
			this.model = model;
			this.builder = builder;

		}

		ParallelTopicModel model;
		TopicModelBuilder builder;
		
		public Alphabet getDataAlphabet() {
			return builder.instances.getDataAlphabet();
		};

		public void printTopics(int terms) {
			//PrintWriter pw = new PrintWriter(System.out);
			TopicModelDiagnostics diag = new TopicModelDiagnostics(model, terms);
			System.out.println(diag.toXML());
			//model.topicPhraseXMLReport(pw, 30);
			//pw.flush();
			//pw.close();
		}

		public int getTopicCount() {
			return builder.topics;
		}
		
		public Stream<Topic> getTopicsForDocuments() {
			
			if (!builder.corpus.hasTopics()) {
				ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		        ArrayList<TreeSet<IDSorter>> topicSortedDocuments = model.getTopicDocuments(10.0);
				Alphabet alphabet = getDataAlphabet();
				
				int[] tokensPerTopic = model.tokensPerTopic;
				
		        // Print results for each topic
		        for (int topic = 0; topic < getTopicCount(); topic++) {
		            TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
		            Topic top = builder.corpus.addTopic(topic);
		            
		            for (IDSorter info: sortedWords) {
		            	//Double weight = info.getWeight();
		            	Double probability = info.getWeight() / tokensPerTopic[topic];
		                String term = alphabet.lookupObject(info.getID()).toString();
		                Term t = builder.corpus.createTermFrom(term);
		                Weighted<Term> wt = Weighted.create(t, probability);
		                top.addTerm(wt);
		            }
		        
		            TreeSet<IDSorter> sortedDocuments = topicSortedDocuments.get(topic);
		            
		            for (IDSorter sorter: sortedDocuments) {
		                
		            	int doc = sorter.getID();
		                double proportion = sorter.getWeight();
		                String name = model.data.get(doc).instance.getName().toString();
		                
		                Optional<Document> optDoc = builder.corpus.getById(name);
		                optDoc.ifPresent(d -> {
		                	top.addDocument(Weighted.create(d, proportion));
		                });
		                
		            }
		        }
			}
			
	        return builder.corpus.streamTopics();
			
			
		}

		//TODO: http://diging.github.io/tethne/tutorial.mallet.html#topic-modeling-in-mallet

		public Map<String, Double> getTopic(int topicNumber) {
			Map<String,Double> out = new HashMap<>();
			LabelSequence ls = model.data.get(topicNumber).topicSequence;
			for (int i=0; i<ls.getLength(); i++) {
				//ls.get
				//out.put(ls.getLabelAtPosition(i)., value)
			}
			return out;
		}

		public Corpus getCorpus() {
			return builder.corpus;
		}
	}


}
