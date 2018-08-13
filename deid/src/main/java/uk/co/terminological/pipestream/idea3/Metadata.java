package uk.co.terminological.pipestream.idea3;

import java.io.Serializable;
import java.util.Optional;

public class Metadata implements Serializable {
	
	String name;
	String typeDescription;
	
	
	public Optional<String> name() {
		return Optional.ofNullable(name);
	};
	public String typeDescription() {
		return typeDescription;
	};
	
	public Metadata(String name, String typeDescription) {
		this.name = name; this.typeDescription = typeDescription;
	}
	
	
}