package uk.co.terminological.pipestream.idea3;

import java.util.Optional;

public interface Event<Y> {
	Optional<String> name();
	String typeDescription();
	Class<Y> getType();
	Y get();
	default boolean multiProcess() {return true;}
	
	public static class Default<Y> implements Event<Y> {

		Y message;
		public Default(Y message) {
			this.message = message;
		};
		
		@Override
		public Optional<String> name() {
			return Optional.of(Integer.toHexString(message.hashCode()));
		}

		@Override
		public String typeDescription() {
			return getType().getCanonicalName();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<Y> getType() {
			return (Class<Y>) message.getClass();
		}

		@Override
		public Y get() {
			return message;
		}
		
	}
}