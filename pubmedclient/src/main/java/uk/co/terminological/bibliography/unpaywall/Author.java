package uk.co.terminological.bibliography.unpaywall;

import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

public class Author extends ExtensibleJson implements uk.co.terminological.bibliography.record.Author {
	
	public Author(JsonNode node) { super(node); }
	
	public String getLastName() {return this.asString("family").orElse(this.asString("name").orElse("Unknown"));}
	public Optional<String> getORCID() {return this.asString("ORCID");}
	public Stream<String> getAffiliations() {return this.streamPath("affiliation","name").map(n -> n.asString());}
	public Optional<String> getFirstName() {return this.asString("given");}
	
	public boolean isFirst() {return this.asString("sequence").filter(s -> s.equals("first")).isPresent();}
	
	public String getLabel() {
		return (getLastName()+", "+getFirstName().orElse("Unknown").substring(0, 1)).toLowerCase();
	}

	@Override
	public Optional<String> getInitials() {
		return getFirstName().map(s -> s.substring(0,1));
	}

	
}