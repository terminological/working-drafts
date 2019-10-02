package uk.co.terminological.bibliography.scholar;

import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;

public class GoogleResult {
	
	private XmlElement raw;
	public GoogleResult(XmlElement raw) throws XmlException {
		this.raw=raw; 
		convert();
	}
	
	private void convert() {
		// TODO Auto-generated method stub
		
	}
	
	public XmlElement raw() {return raw;}
}
