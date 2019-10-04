package uk.co.terminological.bibliography.opencitations;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.text.pdf.StringUtils;

import uk.co.terminological.bibliography.ExtensibleJson;

public class Citation extends ExtensibleJson {
	
	public Citation(JsonNode node) { super(node); }
	
	public Optional<String> getCitingDoi() {return this.asString("citing").map(Citation::value);}
	public Optional<String> getCitedDoi() {return this.asString("cited").map(Citation::value);}
	public Optional<String> getCitingDate() {return this.asString("creation").map(Citation::value);}
	public Optional<String> getCitationId() {return this.asString("oci").map(Citation::value);}
	public Optional<String> getCitationIndex() {return this.asString("oci").map(Citation::key);}
	
	private static String value(String response) {
		if (response.contains(" => "))
			return response.split(" => ")[1];
		else
			return response;
	}
	
	private static String key(String response) {
		if (response.contains(" => "))
			return response.split(" => ")[0];
		else
			return null;
	}
	
}
