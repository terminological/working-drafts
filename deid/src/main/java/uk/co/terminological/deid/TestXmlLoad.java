package uk.co.terminological.deid;
import static uk.co.terminological.deid.Config.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.pipeline.POSTaggerAnnotator;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.util.StringUtils;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlText;

@Deprecated
public class TestXmlLoad {

	public static void main(String[] args) throws XmlException, IOException, ClassCastException, ClassNotFoundException {
		BasicConfigurator.configure();
		
		InputStream in = TestXmlLoad.class.getClassLoader().getResourceAsStream("deid/i2b2example.xml");
		Xml xml = Xml.fromStream(in);
		XmlText tmp = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class);
		System.out.print(tmp.getValue());
		List<Triple<Integer,Integer,String>> types = new ArrayList<Triple<Integer,Integer,String>>();
		
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
			System.out.println("NAME: "+tags.getName());
			System.out.println("ID: "+tags.getAsElement().getAttribute("id"));
			System.out.println("START: "+tags.getAsElement().getAttribute("start"));
			types.add(
					Triple.create(
							Integer.parseInt(tags.getAsElement().getAttribute("start")),
							Integer.parseInt(tags.getAsElement().getAttribute("end")), 
							tags.getName()+"-"+tags.getAsElement().getAttribute("TYPE")));
		};
		Collections.sort(types, new Comparator<Triple<Integer,Integer,String>>() {
			public int compare(Triple<Integer, Integer, String> arg0, Triple<Integer, Integer, String> arg1) {
				return 
						arg0.getFirst().compareTo(arg1.getFirst()) != 0 ?
								arg0.getFirst().compareTo(arg1.getFirst()):
								arg0.getSecond().compareTo(arg1.getSecond());
			}
			;
		});

		// https://stanfordnlp.github.io/CoreNLP/pipelines.html
		
		// Properties props = new Properties();
	    // set the list of annotators to run
	    // props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
		// props.setProperty("annotators", "tokenize,ssplit,pos");
	    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
	    // props.setProperty("coref.algorithm", "neural");
	    // build pipeline
	    // StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		//TODO: this is set up to use a pre trained classifier for NER. Need to build this into a proper experiment.  
	    Properties crfprops = StringUtils.propFileToProperties(PROP);
		crfprops.setProperty("serializeTo", OUTFILE);
		// props.setProperty("trainFile", TRAINING_FILE);
		// props.setProperty("gazette", GAZETTE);
		// props.setProperty("testFile", TESTING_FILE);
		
		// StanfordCoreNLP pl = new StanfordCoreNLP();
		AnnotationPipeline pl = new AnnotationPipeline();
	    pl.addAnnotator(new TokenizerAnnotator(false));
	    pl.addAnnotator(new WordsToSentencesAnnotator(false));
	    pl.addAnnotator(new POSTaggerAnnotator(false));
		CRFClassifier<CoreLabel> crf = CRFClassifier.getClassifier(OUTFILE);
	    NERClassifierCombiner ncc = new NERClassifierCombiner(
	    		crf
	    		
	    		);
	    NERCombinerAnnotator nca = new NERCombinerAnnotator(ncc, true);
	    
	    pl.addAnnotator(nca);
		
		
	    // create a document object
	    CoreDocument document = new CoreDocument(tmp.getValue());
	    // annnotate the document
	    pl.annotate(document.annotation());
	    document.wrapAnnotations();
	    
	    // examples
	    Iterator<Triple<Integer,Integer,String>> typeIt = types.iterator();
	    Triple<Integer,Integer,String> tok = typeIt.next();
	    // 10th token of the document
	    for (CoreSentence sentence: document.sentences()) {
		    for (CoreLabel token: sentence.tokens()) {
		    	
		    	while (tok != null && token.beginPosition() > tok.getSecond()) tok = typeIt.hasNext() ? typeIt.next() : null;
		    	boolean spanning = tok != null && token.endPosition() <= tok.getSecond() && token.beginPosition() >= tok.getFirst();
		    	
		    	System.out.println(
		    			token.originalText()+"\t"+token.beginPosition()+":"+token.endPosition()+"\t"+token.ner()+"\t"
		    			+ (spanning ? tok.getThird() : "0")
		    			 
		    			);
		    };
		    System.out.println();
	    };

	    /*
	    // text of the first sentence
	    String sentenceText = document.sentences().get(0).text();
	    System.out.println("Example: sentence");
	    System.out.println(sentenceText);
	    System.out.println();

	    // second sentence
	    CoreSentence sentence = document.sentences().get(1);

	    // list of the part-of-speech tags for the second sentence
	    List<String> posTags = sentence.posTags();
	    System.out.println("Example: pos tags");
	    System.out.println(posTags);
	    System.out.println();

	    // list of the ner tags for the second sentence
	    List<String> nerTags = sentence.nerTags();
	    System.out.println("Example: ner tags");
	    System.out.println(nerTags);
	    System.out.println();

	    // constituency parse for the second sentence
	    Tree constituencyParse = sentence.constituencyParse();
	    System.out.println("Example: constituency parse");
	    System.out.println(constituencyParse);
	    System.out.println();

	    // dependency parse for the second sentence
	    SemanticGraph dependencyParse = sentence.dependencyParse();
	    System.out.println("Example: dependency parse");
	    System.out.println(dependencyParse);
	    System.out.println();

	    // kbp relations found in fifth sentence
	    List<RelationTriple> relations =
	        document.sentences().get(4).relations();
	    System.out.println("Example: relation");
	    if (relations.size() > 0) System.out.println(relations.get(0));
	    System.out.println();

	    // entity mentions in the second sentence
	    List<CoreEntityMention> entityMentions = sentence.entityMentions();
	    System.out.println("Example: entity mentions");
	    System.out.println(entityMentions);
	    System.out.println();

	    // coreference between entity mentions
	    if (document.sentences().get(3).entityMentions().size() > 0) {
	    CoreEntityMention originalEntityMention = document.sentences().get(3).entityMentions().get(1);
	    System.out.println("Example: original entity mention");
	    System.out.println(originalEntityMention);
	    System.out.println("Example: canonical entity mention");
	    System.out.println(originalEntityMention.canonicalEntityMention().get());
	    System.out.println();
	    }

	    // get document wide coref info
	    Map<Integer, CorefChain> corefChains = document.corefChains();
	    System.out.println("Example: coref chains for document");
	    System.out.println(corefChains);
	    System.out.println();

	    // get quotes in document
	    List<CoreQuote> quotes = document.quotes();
	    if (quotes.size()>0) {
	    	CoreQuote quote = quotes.get(0);
	    	System.out.println("Example: quote");
	    	System.out.println(quote);
	    	System.out.println();


	    	// original speaker of quote
	    	// note that quote.speaker() returns an Optional
	    	System.out.println("Example: original speaker of quote");
	    	System.out.println(quote.speaker().get());
	    	System.out.println();

	    	// canonical speaker of quote
	    	System.out.println("Example: canonical speaker of quote");
	    	System.out.println(quote.canonicalSpeaker().get());
	    	System.out.println();
	    }
	    */
	}
	
}
