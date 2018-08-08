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
	
	static Map<String,? extends List<String>> types = FluentMap
			.with("NAME", create("PATIENT", "DOCTOR", "USERNAME"))
			.and("PROFESSION", empty())
			.and("LOCATION", create("HOSPITAL", "ORGANIZATION", "STREET", "CITY"))
			.and("AGE",empty())
			.and("DATE", empty())
			.and("CONTACT", create("PHONE", "FAX", "EMAIL", "URL", "IPADDRESS"))
			.and("ID", create("SOCIAL SECURITY NUMBER", "MEDICAL RECORD NUMBER", "HEALTH PLAN NUMBER",
					"ACCOUNT NUMBER", "LICENSE NUMBER", "VEHICLE ID", "DEVICE ID", "BIOMETRIC ID", "ID NUMBER"));
		
	
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
		Xml xml = Xml.fromStream(in);
		String documentText = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class).getValue();
		
		// extract existing tags
		List<Triple<Integer,Integer,String>> types = new ArrayList<Triple<Integer,Integer,String>>();
		
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
			// System.out.println("NAME: "+tags.getName());
			// System.out.println("ID: "+tags.getAttributeValue("id"));
			// System.out.println("START: "+tags.getAttributeValue("start"));
			types.add(
					Triple.create(
							Integer.parseInt(tags.getAsElement().getAttribute("start")),
							Integer.parseInt(tags.getAsElement().getAttribute("end")), 
							tags.getName()+"\t"+tags.getAsElement().getAttribute("TYPE")));
		};
		
		//Sort tags ascending
		Collections.sort(types, new Comparator<Triple<Integer,Integer,String>>() {
			public int compare(Triple<Integer, Integer, String> arg0, Triple<Integer, Integer, String> arg1) {
				return 
						arg0.getFirst().compareTo(arg1.getFirst()) != 0 ?
								arg0.getFirst().compareTo(arg1.getFirst()):
								arg0.getSecond().compareTo(arg1.getSecond());
			}
			;
		});
		
		// get tokens
		CoreDocument document = new CoreDocument(documentText);
	    pipeline.annotate(document);
	    
	    // find existing tags examples
	    Iterator<Triple<Integer,Integer,String>> typeIt = types.iterator();
	    
	    Triple<Integer,Integer,String> tok = typeIt.next();
	    for (CoreSentence sentence: document.sentences()) {
		    for (CoreLabel token: sentence.tokens()) {
		    	
		    	while (tok != null && token.beginPosition() > tok.getSecond()) tok = typeIt.hasNext() ? typeIt.next() : null;
		    	
		    	boolean spanning = tok != null && token.endPosition() <= tok.getSecond() && token.beginPosition() >= tok.getFirst();
		    	spanning = spanning && tok.getThird().startsWith("NAME");
		    	
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
