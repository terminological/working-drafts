package uk.co.terminological.deid;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;

public class TestCommonFormatConverter {

	public static void main(String[] args) throws XmlException {
		
		Xml xml = Xml.fromStream(TestCommonFormatConverter.class.getResourceAsStream("/deid_surrogate_test_all_groundtruth_version2.xml"));
		CommonFormat.Records rec = new CommonFormatConverter().fromI2B2_2006_Xml(xml);
		CommonFormat.Record r = rec.stream().findFirst().get();
		r.spans.forEach(span -> 
				System.out.println(
						span.start+"-"+span.start+" "+r.documentText.substring(span.start, span.end))
				);

	}

}
