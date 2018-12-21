package uk.co.terminological.pubmedclient;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ExtensibleJson {
	
	static final Logger logger = LoggerFactory.getLogger(ExtensibleJson.class);
	
	private JsonNode raw;
	protected void setRaw(JsonNode raw) {
		this.raw = raw;
	}
	public JsonNode getRaw() {return raw;}
	
	public ExtensibleJson(JsonNode node) {
		this.raw = node;
	}
	
	public Stream<ExtensibleJson> streamNode(String key) {
		JsonNode node = raw.get(key); 
		if (node.isNull() | node.isMissingNode()) return Stream.empty();
		if (node.isArray()) return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(node.elements(), Spliterator.ORDERED),false)
				.map(s -> new ExtensibleJson(s))
				;
		else return Stream.of(new ExtensibleJson(node));
	}
	
	public Stream<ExtensibleJson> streamPath(String... keys) {
		Stream<ExtensibleJson> out = Stream.of(this);
		for (String key: keys) {
			out = out.flatMap(t -> t.streamNode(key));
		}
		return out;
	}
	
	public Optional<String> asString(String key) {
		return Optional.ofNullable(raw.get(key).asText(null));
	}
	
	public Optional<Double> asDouble(String key) {
		if (!raw.has(key)) return Optional.empty();
		JsonNode tmp = raw.get(key);
		if (!tmp.isDouble()) return Optional.empty(); 
		return Optional.of(raw.get(key).asDouble());
	}
	
	public Optional<Long> asLong(String key) {
		if (!raw.has(key)) return Optional.empty();
		JsonNode tmp = raw.get(key);
		if (!tmp.canConvertToLong()) return Optional.empty(); 
		return Optional.of(raw.get(key).asLong());
	}
	
	
	
	/*Map<String,Object> unknownProperties = new HashMap<>();
	
	@JsonAnySetter
    public void handleUnknownProperty(String key, Object value) {
        logger.debug(getClass().getCanonicalName()+" - unknown property: {}: {}", key, value.toString());
        unknownProperties.put(key, value);
    }
	
	public Object getUnknownProperty(String key) {
		return unknownProperties.get(key);
	}*/
}