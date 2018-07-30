package uk.co.terminological.deid;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreQuote;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.Tree;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlNode;
import uk.co.terminological.fluentxml.XmlText;

public class TestXmlLoad {

	public static void main(String[] args) throws XmlException {
		BasicConfigurator.configure();
		
		InputStream in = TestXmlLoad.class.getClassLoader().getResourceAsStream("deid/i2b2example.xml");
		Xml xml = Xml.fromStream(in);
		XmlText tmp = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class);
		System.out.print(tmp.getValue());
		Map<Tuple<Integer,Integer>,String> types = new HashMap<>();
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
			System.out.println("NAME: "+tags.getName());
			System.out.println("ID: "+tags.getAttributeValue("id"));
			System.out.println("START: "+tags.getAttributeValue("start"));
			types.put(Tuple.create(
					Integer.parseInt(tags.getAttributeValue("start")),
					Integer.parseInt(tags.getAttributeValue("end"))
					), tags.getName()+"-"+tags.getAttributeValue("TYPE"));
		};

		
		Properties props = new Properties();
	    // set the list of annotators to run
	    // props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
		props.setProperty("annotators", "tokenize,ssplit,ner");
	    // set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
	    props.setProperty("coref.algorithm", "neural");
	    // build pipeline
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    // create a document object
	    CoreDocument document = new CoreDocument(tmp.getValue());
	    // annnotate the document
	    pipeline.annotate(document);
	    // examples

	    // 10th token of the document
	    for (CoreLabel token: document.tokens()) {
	    	System.out.println(
	    			token.originalText()+"\t"+token.beginPosition()+":"+token.endPosition()+"\t"+token.ner()+"\t"
	    			+
	    			types.get(Tuple.create(token.beginPosition(),token.endPosition()))
	    			);
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
