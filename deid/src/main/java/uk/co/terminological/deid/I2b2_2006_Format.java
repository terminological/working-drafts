package uk.co.terminological.deid;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Records;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlNode;
import uk.co.terminological.fluentxml.XmlText;

public class I2b2_2006_Format {

	Xml xml;
	Records records = new Records();
	
	public I2b2_2006_Format(InputStream is) throws XmlException {
		xml = Xml.fromStream(is);
		for (XmlElement el : xml.doXpath("/ROOT/RECORD").getMany(XmlElement.class)) {
			Record record = new Record();
			record.id = el.getAttributeValue("ID");
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
	}
	
	public Iterator<Record> getRecords() {
		return records.iterator();
	}
	
}
