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
		// /html/body/div/div[11]/div[2]/div[2]/div[2]/div/div[2]
		// document.querySelector("#gs_res_ccl_mid > div > div.gs_ri")
		// #gs_res_ccl_mid > div > div.gs_ri
		
	}
	
	public XmlElement raw() {return raw;}
}
