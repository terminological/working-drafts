package uk.co.terminological.pipestream.idea3;

import java.util.Optional;

public interface Event<Y> extends Cloneable {
	Optional<String> name();
	String typeDescription();
	Class<Y> getType();
	Y getCopy();
}