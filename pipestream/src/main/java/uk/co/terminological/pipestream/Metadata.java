package uk.co.terminological.pipestream;

import java.io.Serializable;
import java.util.Optional;

/**
 * The base metadata class enforces a optional name (for the instance) a type description (for the class of event or handler). 
 * It also tracks the time of creation.
 * @author robchallen
 *
 */
public class Metadata implements Serializable {
	
	String name;
	String typeDescription;
	long timestamp;
	
	/**
	 * Some events may wish to 
	 * @return
	 */
	public Optional<String> name() {
		return Optional.ofNullable(name);
	};
	
	/**
	 * All events or handlers described must define their type. This could be in the form of a urn for example 
	 * @return
	 */
	public String typeDescription() {
		return typeDescription;
	};
	
	/**
	 * Create metadata for something that is only described by type. This works for data processors, such as {@link EventGenerator}s or {@link EventHandler}s.  
	 * @param typeDescription
	 */
	public Metadata(String typeDescription) {
		this(null,typeDescription);
	}
	
	public Metadata(String name, String typeDescription) {
		this.name = name; 
		this.typeDescription = typeDescription;
		this.timestamp = System.currentTimeMillis();
	}
	
	/**
	 * The default string representation includes an optional name, type description and creation timestamp.
	 */
	public String toString() {
		return (name==null ? "" : (name+"@"))
				+typeDescription+" ["+timestamp+"]";
	}
	
	/**
	 * The time this metadata object was created
	 * @return
	 */
	public long timestamp() {
		return timestamp;
	}
	
	
	
}