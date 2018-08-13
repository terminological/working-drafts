package uk.co.terminological.pipestream.idea3;

import java.io.Serializable;
import java.util.Optional;

public class Metadata<Y> implements Serializable {
	
	String name;
	String typeDescription;
	Class<Y> type;
	boolean multiProcess;
	
	public Optional<String> name() {
		return Optional.ofNullable(name);
	};
	public String typeDescription() {
		return typeDescription;
	};
	public Class<Y> getType(){
		return type;
	};
	public boolean multiProcess() {
		return multiProcess;
	}
	
	public Metadata(String name, String typeDescription, Class<Y> type,	boolean multiProcess) {
		this.name = name; this.typeDescription = typeDescription; this.type = type; this.multiProcess = multiProcess;
	}
	
	@SuppressWarnings("unchecked")
	public static <Z> Metadata<Z> defaultFor(Z instance) {
		Metadata<Z> out = new Metadata<Z>(
				Integer.toHexString(instance.hashCode()),
				instance.getClass().getCanonicalName(),
				(Class<Z>) instance.getClass(),
				true);
		return out;
	}
}