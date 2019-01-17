package uk.co.terminological.bibliography.crossref;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

public class ListResult extends ExtensibleJson {
	
	public ListResult(JsonNode node) { super(node); }
	
	public Optional<String> getStatus() {return this.asString("status");}
	public Message getMessage() {return this.asObject(Message.class, "message").get();}
	
}