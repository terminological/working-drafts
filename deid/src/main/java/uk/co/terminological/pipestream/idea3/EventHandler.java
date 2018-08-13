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
		
		
	}
	
	
	public static abstract class Default<X extends Event<?>> implements EventHandler<X> {

		HandlerMetadata metadata;
		
		public HandlerMetadata getMetadata() {
			return metadata;
		};
		
		@SuppressWarnings("unchecked")
		public Default() {
			metadata = HandlerMetadata.defaultFor(this);
			getEventBus().registerHandler((EventHandler<Event<?>>) this);
		}

		
	}
	
	
}