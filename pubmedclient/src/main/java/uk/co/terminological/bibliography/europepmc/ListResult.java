package uk.co.terminological.bibliography.europepmc;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

public class ListResult<X extends ExtensibleJson> extends ExtensibleJson {

	Class<X> clazz;
	
	private ListResult(Class<X> clazz, JsonNode raw) {
		super(raw);
		this.clazz = clazz;
	}
	
	public static <Y extends ExtensibleJson> ListResult<Y> create(Class<Y> clazz, JsonNode raw) {
		return new ListResult<Y>(clazz,raw);
	}
	
	
}
