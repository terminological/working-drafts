package uk.co.terminological.deid;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;

public class TestCommonFormatConverter {

	public static void main(String[] args) throws XmlException {
		
		Xml xml = Xml.fromStream(TestCommonFormatConverter.class.getResourceAsStream("deid_surrogate_test_all_groundtruth_version2.xml"));
		CommonFormat.Records rec = new CommonFormatConverter().fromI2B2_2006_Xml(xml);
		rec.stream().findFirst().get().spans.forEach(System.out::println);

	}

}
