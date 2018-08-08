package uk.co.terminological.deid;

import static uk.co.terminological.datatypes.FluentList.create;
import static uk.co.terminological.datatypes.FluentList.empty;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.datatypes.Triple;
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
	String documentText;
	Spans spans = new Spans();
	
	public I2b2_2014_Format(InputStream is) throws XmlException {
		xml = Xml.fromStream(is);
		documentText = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class).getValue();
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
			spans.add(
					Span.from(
							Integer.parseInt(tags.getAsElement().getAttribute("start")),
							Integer.parseInt(tags.getAsElement().getAttribute("end")), 
							tags.getName(),
							tags.getAsElement().getAttribute("TYPE")));
		};
		Collections.sort(spans);
		
	}
	
	public String getText() {
		return documentText;
	}
	
	public Spans getMarkup() {
		return spans;
	}
		
	public static class Spans extends FluentList<Span> {}
	
	public static class Span implements Comparable<Span> {
		Integer start;
		Integer end;
		String type;
		String subtype;
		
		public static Span from(Integer start, Integer end, String type, String subtype) {
			Span out = new Span(); out.start = start; out.end = end; out.type = type; out.subtype = subtype; return out;
		}

		@Override
		public int compareTo(Span that) {
			return this.start.compareTo(that.start) != 0 ?
					this.start.compareTo(that.start) :
						this.end.compareTo(that.end);
		}
		
		public boolean intersects(Integer start, Integer end) {
			return (this.start <= start && this.end >= start) ||
					(this.start <= end && this.end >= end);
		}
		
		public boolean after(Integer end) {
			return (this.start > end);
		}
		
		public boolean before(Integer start) {
			return (this.end < start);
		}
	}
}
