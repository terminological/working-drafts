package uk.co.terminological.deid;

import java.io.InputStream;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;

public class TestXmlLoad {

	public static void main(String[] args) throws XmlException {
		
		InputStream in = TestXmlLoad.class.getClassLoader().getResourceAsStream("deid/i2b2example.xml");
		Xml xml = Xml.fromStream(in);
		System.out.print(xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne().toString());
		
	}
	
}
