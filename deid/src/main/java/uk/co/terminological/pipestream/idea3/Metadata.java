package uk.co.terminological.pipestream.idea3;

import java.io.Serializable;
import java.util.Optional;

public class Metadata<Y> implements Serializable {
	
	String name;
	String typeDescription;
	Class<Y> type;
	
	
	public Optional<String> name() {
		return Optional.ofNullable(name);
	};
	public String typeDescription() {
		return typeDescription;
	};
	public Class<Y> getType(){
		return type;
	};
	
	
	public Metadata(String name, String typeDescription, Class<Y> type) {
		this.name = name; this.typeDescription = typeDescription; this.type = type;
	}
	
	
}