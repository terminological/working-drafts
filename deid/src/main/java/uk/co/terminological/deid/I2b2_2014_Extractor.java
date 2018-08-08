package uk.co.terminological.deid;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.deid.CommonFormat.Spans;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlText;

import static uk.co.terminological.datatypes.FluentList.*;

/**
 * Extract sentences / tokenise and match up existing resources from  
 */
public class I2b2_2014_Extractor {
	
	StanfordCoreNLP pipeline;
	
	static Logger log = LoggerFactory.getLogger(I2b2_2014_Extractor.class);
	
	
		
	
	public I2b2_2014_Extractor() {
		
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos");
	    pipeline = new StanfordCoreNLP(props);
	    
	}
	
	public void convert(ArchiveInputStream zipIn, Writer out) throws XmlException, IOException {
		//public void readZipStream(InputStream in) throws IOException {
		ArchiveEntry entry;
		while ((entry = zipIn.getNextEntry()) != null) {
		    if (entry.getName().endsWith("xml")) {
		    	log.debug("processing: "+entry.getName());

		    	// this stops Xml from closing the underlynig ZipInputStream
		    	InputStream tmp = new FilterInputStream(zipIn) {
		            @Override
		            public void close() throws IOException {
		                // do nothing!
		            }
		        };
		    	convert(tmp,out);
		    }
		}
	}
	
	/**
	 * flushes writer but does not close it
	 * @param in
	 * @param out
	 * @throws XmlException - if it can't parse the XML
	 * @throws IOException - if it can't write to the output
	 */
	public void convert(InputStream in, Writer out) throws XmlException, IOException {
		I2b2_2014_Format infile = new I2b2_2014_Format(in); 
		
		// get document as tokens
		CoreDocument document = new CoreDocument(infile.getText());
	    pipeline.annotate(document);
	    
	    // find existing tags examples
	    Iterator<Span> typeIt = infile.getMarkup().iterator();
	    Span span = typeIt.next();
	    
	    for (CoreSentence sentence: document.sentences()) {
		    for (CoreLabel token: sentence.tokens()) {
		    	
		    	while (span != null && span.before(token.beginPosition())) 
		    		span = typeIt.hasNext() ? typeIt.next() : null;
		    	
		    	boolean spanning = span != null && span.intersects(token.beginPosition(), token.endPosition());
		    	spanning = spanning && span.isType("NAME");
		    	
		    	out.append(
		    			token.originalText()+"\t"
		    					//+token.beginPosition()+":"+token.endPosition()+"\t"+token.ner()+"\t"
		    			+ (spanning ? tok.getThird() : "O\tO") + System.lineSeparator() 	);
		    };
		    out.append(System.lineSeparator());
	    };
	    out.append(System.lineSeparator());
		out.flush();
		
	}

}
