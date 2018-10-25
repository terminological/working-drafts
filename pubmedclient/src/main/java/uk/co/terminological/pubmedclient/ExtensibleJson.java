package uk.co.terminological.pubmedclient;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ExtensibleJson {
	
	static final Logger logger = LoggerFactory.getLogger(ExtensibleJson.class);
	
	Map<String,Object> unknownProperties = new HashMap<>();
	
	@JsonAnySetter
    public void handleUnknownProperty(String key, Object value) {
        logger.debug("Unknown property: {}: {}", key, value.toString());
        unknownProperties.put(key, value);
    }
	
	public Object getUnknownProperty(String key) {
		return unknownProperties.get(key);
	}
}