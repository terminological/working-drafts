package uk.co.terminological.deid;

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
import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Records;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlNode;
import uk.co.terminological.fluentxml.XmlText;

public class CommonFormatConverter {

	StanfordCoreNLP pipeline;
	static Logger log = LoggerFactory.getLogger(CommonFormatConverter.class);
	
	StanfordCoreNLP pipeline() {
		if (pipeline == null) {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize,ssplit,pos");
			pipeline = new StanfordCoreNLP(props);
		}
		return pipeline;
	}
	
	public CoNLL_2003.List toCoNLL_2003(CommonFormat.Record record) {
		
		CoreDocument document = new CoreDocument(record.documentText);
	    pipeline().annotate(document);
	    
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
	
	public Record fromI2B2_2014_Xml(Xml xml, String id) throws XmlException {
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


	public Records fromI2B2_2006_Xml(Xml xml, String filename) throws XmlException {
		Records records = new Records();
		for (XmlElement el : xml.doXpath("/ROOT/RECORD").getMany(XmlElement.class)) {
			Record record = new Record();
			record.id = filename+"/"+el.getAttributeValue("ID");
			record.documentText = el.getAsElement().getTextContent();
			StringBuilder docText = new StringBuilder();
			for (XmlNode phiEl : el.walkTree()) {
				if (phiEl.is(XmlText.class)) {
					docText.append(phiEl.as(XmlText.class).getValue());
				} else if (phiEl.is(XmlElement.class)) {
					XmlElement tmp = phiEl.as(XmlElement.class); 
					if (tmp.getName().equals("PHI")) {
						Integer start = docText.length();
						Integer end = tmp.getAsElement().getTextContent().length()+start;
					Span span = Span.from(
							start, end, 
							tmp.getAttributeValue("TYPE"),
							null);
					record.spans.add(span);
					}
				}
				
			}
			Collections.sort(record.spans);
			records.add(record);
		}
		return records;
	}
	
	public BRATFormat toBRATFormat(Record record) {
		BRATFormat out = BRATFormat.create(record.documentText, record.id);
		record.spans.forEach(
				span -> out
					.withAnnotation(
						BRATFormat.Annotation.textBound(
								span.type, span.start, span.end, 
								record.documentText.substring(span.start, span.end)
								))
					.withAnnotation(
							BRATFormat.Annotation.textBound(
									span.subtype, span.start, span.end, 
									record.documentText.substring(span.start, span.end)
									))
				);
		return out;
		
	}
	
	
	
}
