package uk.co.terminological.deid;

import java.io.Writer;
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
	
	public void writeToCoNLL_2003(CommonFormat.Record record, Writer out) {
		
		CoreDocument document = new CoreDocument(record.documentText);
	    pipeline.annotate(document);
	    
	    CoNLL_2003.Printer print = new CoNLL_2003.Printer(out); 
	    
	    // find existing tags examples
	    Iterator<Span> typeIt = record.spans.iterator();
	    Span span = typeIt.next();
	    
	    for (CoreSentence sentence: document.sentences()) {
	    	for (CoreLabel token: sentence.tokens()) {
		    	
		    	while (span != null && span.before(token.beginPosition())) 
		    		span = typeIt.hasNext() ? typeIt.next() : null;
		    	
		    	boolean spanning = span != null && span.intersects(token.beginPosition(), token.endPosition());
		    	print.addLine(CoNLL_2003.Entry.entry(
		    			token.originalText(), 
		    			token.getString(CoreAnnotations.PartOfSpeechAnnotation.class),
		    			token.getString(CoreAnnotations.ChunkAnnotation.class),
		    			spanning ? span.type : "0"));
		    			
		    }
		    print.addBlank();
	    };
	    print.addBlank();
		
	}
	
}
