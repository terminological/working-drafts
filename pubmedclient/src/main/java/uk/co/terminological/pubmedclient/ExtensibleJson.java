package uk.co.terminological.pubmedclient;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class ExtensibleJson {
	
	Map<String,Object> unknownProperties = new HashMap<>();
	
	@JsonAnySetter
    public void handleUnknownProperty(String key, Object value) {
        CrossRefApiResponse.logger.debug("Unknown property: {}: {}", key, value.toString());
        unknownProperties.put(key, value);
    }
	
	public Object getUnknownProperty(String key) {
		return unknownProperties.get(key);
	}
}