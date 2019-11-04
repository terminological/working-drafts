package uk.co.terminological.bibliography.crossref;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.record.Raw;

public class SingleResult extends ExtensibleJson implements Raw<JsonNode> {
	public SingleResult(JsonNode node) { super(node); }
	
	public Optional<String> getStatus() {return this.asString("status");}
	public Work getWork() {return this.asObject(Work.class, "message").get();}
	
}