package uk.co.terminological.deid;

import java.io.InputStream;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.deid.CommonFormat.Spans;

public class I2b2_2006_Format {

	Xml xml;
	
	public I2b2_2006_Format(InputStream is) throws XmlException {
		xml = Xml.fromStream(is);
	}
	
	
	
}
