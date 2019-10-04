package uk.co.terminological.bibliography.opencitations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.crossref.Message;

public class ListResult extends ExtensibleJson {
	
	public ListResult(JsonNode node) { super(node); }
	
	public Stream<Citation> getCitations() {return 
			this.streamNode(Citation.class);
	}
	
}
