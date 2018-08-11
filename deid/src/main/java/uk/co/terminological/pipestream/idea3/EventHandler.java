package uk.co.terminological.pipestream.idea3;

public interface EventHandler<X extends Event<?>> extends EventBusAware {
	boolean canHandle(Event<?> event);
	void handle(X event);
	
	public static abstract class Default<X extends Event<?>> implements EventHandler<X> {

		@SuppressWarnings("unchecked")
		public Default() {
			getEventBus().registerHandler((EventHandler<Event<?>>) this); 
		}

		
	}
	
	
}