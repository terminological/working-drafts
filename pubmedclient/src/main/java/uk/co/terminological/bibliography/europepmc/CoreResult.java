package uk.co.terminological.bibliography.europepmc;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

public class CoreResult extends ExtensibleJson {

	//https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=DOI:10.1073/pnas.0506580102&sort=CITED%20desc&format=json&resultType=core
	
	public CoreResult(JsonNode node) { super(node); }
}
