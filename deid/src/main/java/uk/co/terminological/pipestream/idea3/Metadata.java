package uk.co.terminological.pipestream.idea3;

import java.io.Serializable;
import java.util.Optional;

import uk.co.terminological.pipestream.idea3.EventHandler.HandlerMetadata;

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
	
	public static Metadata basic(String typeDescription) {
		return new HandlerMetadata(null,typeDescription);
	}
	
	public static Metadata named(String name, String typeDescription) {
		return new HandlerMetadata(name,typeDescription);
	}
	
}