package uk.co.terminological.bibliography.entrez;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.fluentxml.XmlElement;

public class MeshHeading {
	private XmlElement raw;
	public MeshHeading(XmlElement raw) {this.raw = raw;}
	public MeshCode getDescriptor() {
		return raw.childElements("DescriptorName").stream().findFirst().map(o -> new MeshCode(o)).get();
	}
	public Stream<MeshCode> getQualifiers() {
		return raw.childElements("QualifierName").stream().map(o -> new MeshCode(o));
	}
	public String toString() { 
		return getDescriptor().toString()+ " ["+getQualifiers().map(q -> q.toString()).collect(Collectors.joining("; "))+"]";
	}
}