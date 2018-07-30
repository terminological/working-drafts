package uk.co.terminological.deid;

import java.io.InputStream;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlNode;
import uk.co.terminological.fluentxml.XmlText;

public class TestXmlLoad {

	public static void main(String[] args) throws XmlException {
		
		InputStream in = TestXmlLoad.class.getClassLoader().getResourceAsStream("deid/i2b2example.xml");
		Xml xml = Xml.fromStream(in);
		XmlText tmp = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class);
		System.out.print(tmp.getValue());
		for (XmlElement tags: xml.doXpath("/deIdi2b2/TEXT[1]/TAGS").getMany(XmlElement.class)) {
			System.out.print("NAME: "+tags.getName());
			System.out.print("ID: "+tags.getAttributeValue("id"));
			System.out.print("ID: "+tags.getAttributeValue("start"));
		};
		
	}
	
}
