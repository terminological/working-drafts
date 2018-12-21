package uk.co.terminological.pubmedclient;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;
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