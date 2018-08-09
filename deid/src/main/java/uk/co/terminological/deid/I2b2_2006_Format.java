package uk.co.terminological.deid;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Records;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
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
			el.
			for (XmlElement phiEl : el.doXpath("PHI").getMany(XmlElement.class)) {
				Integer start = phiEl.doXpath("preceding::text()").getManyAsStream(XmlText.class)
							.collect(Collectors.summingInt(xt -> xt.getValue().length()));
				Integer end = start+phiEl.getAsElement().getTextContent().length();
				Span span = Span.from(
						start, end, 
						phiEl.getAttributeValue("TYPE"),
						null);
				record.spans.add(span);
			}
			Collections.sort(record.spans);
			records.add(record);
		}
	}
	
	public Iterator<Record> getRecords() {
		return records.iterator();
	}
	
}
