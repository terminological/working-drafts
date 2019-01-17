package uk.co.terminological.bibliography.entrez;

import uk.co.terminological.fluentxml.XmlElement;

public class MeshCode {
	private XmlElement raw;
	public MeshCode(XmlElement raw) {this.raw = raw;}
	public String getCode() { return raw.getAttributeValue("UI").get(); }
	public String getTerm() { return raw.getTextContent().get(); }
	public String toString() {return getCode()+":"+getTerm();}
}