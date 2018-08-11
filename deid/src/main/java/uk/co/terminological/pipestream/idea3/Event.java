package uk.co.terminological.pipestream.idea3;

import java.util.Optional;

public interface Event<Y> {
	Optional<String> name();
	String typeDescription();
	Class<Y> getType();
	Y get();
	default boolean multiProcess() {return true;}
	
	public static abstract class Default<Y> implements Event<Y> {

		@Override
		public Optional<String> name() {
			return Optional.of(Integer.toHexString(get().hashCode()));
		}

		@Override
		public String typeDescription() {
			return getType().getCanonicalName();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Y> getType() {
			return (Class<Y>) get().getClass();
		}

		
		
	}
}