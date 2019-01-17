package uk.co.terminological.bibliography.crossref;

import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

//When message-type is work-list
public class Message extends ExtensibleJson {
	public Message(JsonNode node) { super(node); }
	
	public Optional<String> getNextCursor() {return this.asString("next-cursor");}
	public Stream<Work> getItems() {return this.streamNode(Work.class, "items");}
	public Optional<Long> getTotalResults() {return this.asLong("total-results");}
	
}