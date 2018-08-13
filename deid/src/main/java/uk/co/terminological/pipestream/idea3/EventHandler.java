package uk.co.terminological.pipestream.idea3;

public interface EventHandler<X extends Event<?>> extends EventBusAware {
	
	
	boolean canHandle(Event<?> event);
	void handle(X event);
	HandlerMetadata getMetadata();
	
	
	public static class HandlerMetadata extends Metadata {
		
		public HandlerMetadata(String name, String typeDescription) {
			super(name,typeDescription); 
		}

		public static HandlerMetadata defaultFor(EventHandler<?> instance) {
			HandlerMetadata out = new HandlerMetadata(
					Integer.toHexString(instance.hashCode()),
					instance.getClass().getCanonicalName());
			return out;
		}
		
		public static HandlerMetadata basic(String typeDescription) {
			return new HandlerMetadata(null,typeDescription);
		}
		
		public static HandlerMetadata named(String name, String typeDescription) {
			return new HandlerMetadata(name,typeDescription);
		}
	}
	
	
	public static abstract class Default<X extends Event<?>> implements EventHandler<X> {

		private HandlerMetadata metadata;
		
		public HandlerMetadata getMetadata() {
			return metadata;
		};
		
		@SuppressWarnings("unchecked")
		public Default() {
			metadata = HandlerMetadata.defaultFor(this);
			getEventBus().registerHandler((EventHandler<Event<?>>) this);
		}
		
		@SuppressWarnings("unchecked")
		public Default(HandlerMetadata metadata) {
			this.metadata = metadata;
			getEventBus().registerHandler((EventHandler<Event<?>>) this);
		}

		
	}
	
	
}