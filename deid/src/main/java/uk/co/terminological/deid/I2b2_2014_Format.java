package uk.co.terminological.deid;

import static uk.co.terminological.datatypes.FluentList.create;
import static uk.co.terminological.datatypes.FluentList.empty;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.deid.CommonFormat.Spans;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlText;

public class I2b2_2014_Format {

	static Map<String,? extends List<String>> elementSubtypes = FluentMap
			.with("NAME", create("PATIENT", "DOCTOR", "USERNAME"))
			.and("PROFESSION", empty())
			.and("LOCATION", create("HOSPITAL", "ORGANIZATION", "STREET", "CITY", "STATE", "COUNTRY", "ZIP", "LOCATION-OTHER"))
			.and("AGE",empty())
			.and("DATE", empty())
			.and("CONTACT", create("PHONE", "FAX", "EMAIL", "URL", "IPADDR"))
			.and("ID", create("SSN", "MEDICALRECORD", "HEALTHPLAN", "ACCOUNT", "LICENSE", "VEHICLE", "DEVICE", "BIOID", "IDNUM"));
	
	Xml xml;
	Record record;
	
	public I2b2_2014_Format(InputStream is) throws XmlException {
		xml = Xml.fromStream(is);
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
		
	}
	
	public String getText() {
		return record.documentText;
	}
	
	public Spans getMarkup() {
		return record.spans;
	}
		
	
}
