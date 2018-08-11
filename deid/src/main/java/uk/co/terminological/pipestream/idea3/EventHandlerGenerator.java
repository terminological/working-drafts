package uk.co.terminological.pipestream.idea3;

public interface EventHandlerGenerator<X extends Event<?>> extends EventBusAware {
	boolean canCreateHandler(X event);
	EventHandler<X> createHandlerAndHandle(X event);
	
	public static abstract class Default<X extends Event<?>> implements EventHandlerGenerator<X> {

		@SuppressWarnings("unchecked")
		public Default() {
			getEventBus().registerHandlerGenerator((EventHandlerGenerator<Event<?>>) this); 
		}

		
	}
	
}