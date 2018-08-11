package uk.co.terminological.pipestream.idea3;

public interface EventHandlerGenerator<X extends Event<?>> extends EventBusAware {
	boolean canCreateHandler(X event);
	EventHandler<X> createHandlerAndHandle(X event);
	
	public static abstract class Default<X extends Event<?>> implements EventHandlerGenerator<X> {

		EventBus bus;
		
		@Override
		public EventBus getEventBus() {
			return bus;
		}

		@Override
		public void setEventBus(EventBus eventBus) {
			this.bus = eventBus;
		}
		
		@SuppressWarnings("unchecked")
		public Default() {
			EventBus.get().registerHandlerGenerator((EventHandlerGenerator<Event<?>>) this); 
		}

		
	}
	
}