package uk.co.terminological.pipestream.idea3;

public interface EventHandler<X extends Event<?>> extends EventBusAware {
	
	
	boolean canHandle(Event<?> event);
	void handle(X event);
	HandlerMetadata<? extends EventHandler<X>> getMetadata();
	
	
	public static class HandlerMetadata<Y> extends Metadata<Y> {
		
		public HandlerMetadata(String name, String typeDescription, Class<Y> type) {
			super(name,typeDescription,type); 
		}

		@SuppressWarnings("unchecked")
		public static <Z> HandlerMetadata<Z> defaultFor(Z instance) {
			HandlerMetadata<Z> out = new HandlerMetadata<Z>(
					Integer.toHexString(instance.hashCode()),
					instance.getClass().getCanonicalName(),
					(Class<Z>) instance.getClass());
			return out;
		}
	}
	
	
	public static abstract class Default<X extends Event<?>> implements EventHandler<X> {

		HandlerMetadata<? extends Default<X>> metadata;
		
		public HandlerMetadata<? extends Default<X>> getMetadata() {
			return metadata;
		};
		
		@SuppressWarnings("unchecked")
		public Default() {
			metadata = HandlerMetadata.defaultFor(this);
			getEventBus().registerHandler((EventHandler<Event<?>>) this);
		}

		
	}
	
	
}