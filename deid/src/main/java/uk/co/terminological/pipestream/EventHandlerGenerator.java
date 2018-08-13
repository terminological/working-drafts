package uk.co.terminological.pipestream;

public interface EventHandlerGenerator<X extends Event<?>> extends EventBusAware {
	
	boolean canCreateHandler(Event<?> event);
	EventHandler<X> createHandlerFor(X event);
	
	public static abstract class Default<X extends Event<?>> implements EventHandlerGenerator<X> {

		public Default() {
			
		}
		
	}
	
}