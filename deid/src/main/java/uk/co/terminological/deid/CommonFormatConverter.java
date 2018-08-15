package uk.co.terminological.deid;

import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlText;

public class CommonFormatConverter {

	StanfordCoreNLP pipeline;
	
	static Logger log = LoggerFactory.getLogger(CommonFormatConverter.class);
	public static CommonFormatConverter singleton;
	
	public static CommonFormatConverter get() {
		if (singleton == null) singleton = new CommonFormatConverter();
		return singleton;
	}
	
	CommonFormatConverter() {
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	public CoNLL_2003.List toCoNLL_2003(CommonFormat.Record record) {
		
		CoreDocument document = new CoreDocument(record.documentText);
	    pipeline.annotate(document);
	    
	    CoNLL_2003.List list = new CoNLL_2003.List(); 
	    
	    // find existing tags examples
	    Iterator<Span> typeIt = record.spans.iterator();
	    Span span = typeIt.next();
	    
	    for (CoreSentence sentence: document.sentences()) {
	    	list.addComment(sentence.text());
	    	for (CoreLabel token: sentence.tokens()) {
		    	
		    	while (span != null && span.before(token.beginPosition())) 
		    		span = typeIt.hasNext() ? typeIt.next() : null;
		    	
		    	boolean spanning = span != null && span.intersects(token.beginPosition(), token.endPosition());
		    	list.addLine(CoNLL_2003.Entry.entry(
		    			token.originalText(), 
		    			token.getString(CoreAnnotations.PartOfSpeechAnnotation.class),
		    			token.getString(CoreAnnotations.ChunkAnnotation.class),
		    			spanning ? span.type : "0"));
		    			
		    }
		    list.addBlank();
	    };
	    list.addBlank();
	    
	    return list;
	}
	
	public CommonFormat.Record fromI2B22014Xml(Xml xml, String id) throws XmlException {
		CommonFormat.Record record = new CommonFormat.Record();
		record.id = id;
		record.documentText = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class).getValue();
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
			record.spans.add(
					Span.from(
							Integer.parseInt(tags.getAsElement().getAttribute("start")),
							Integer.parseInt(tags.getAsElement().getAttribute("end")), 
							tags.getName(),
							tags.getAsElement().getAttribute("TYPE")));
		};
		Collections.sort(record.spans);
		return record;
	}
	
}
