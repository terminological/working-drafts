package uk.co.terminological.bibliography.europepmc;

import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;

public class ListResult<X extends ExtensibleJson> extends ExtensibleJson {

	Class<X> clazz;
	
	private ListResult(Class<X> clazz, JsonNode raw) {
		super(raw);
		this.clazz = clazz;
	}
	
	// there paths are either/or in the output.
	public Stream<X> getItems() {
		return Stream.concat(Stream.concat(
				this.streamPath(clazz,"referenceList","reference"),
				this.streamPath(clazz,"citationList","citation")),
				this.streamPath(clazz,"resultList","result")
				);
		
	}
	
	public Optional<String> nextCursorMark() {
		return this.asString("nextCursorMark");}
	
	public Optional<Long> hitCount() {
		return this.asLong("hitCount");}
	
	public Optional<Long> pageSize() {
		return this.streamPath("request","pageSize").findFirst().map(jn -> jn.asLong());}
	
	public Optional<Long> pageNumber() {
		return this.streamPath("request","page").findFirst().map(jn -> jn.asLong());}
	
	public static class Lite extends ListResult<LiteResult> {
		public Lite(JsonNode raw) {
			super(LiteResult.class,raw);
		}
	}
	
	public static class Core extends ListResult<CoreResult> {
		public Core(JsonNode raw) {
			super(CoreResult.class,raw);
		}
	}
	
	public static class Reference extends ListResult<Reference> {
		public Reference(JsonNode raw) {
			super(Reference.class,raw);
		}
	}
	
	public static class Citation extends ListResult<Citation> {
		public Citation(JsonNode raw) {
			super(Citation.class,raw);
		}
	}
}
