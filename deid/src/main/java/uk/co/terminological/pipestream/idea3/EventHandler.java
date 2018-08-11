package uk.co.terminological.pipestream.idea3;

public interface EventHandler<X extends Event<?>> extends EventBusAware {
	boolean canHandle(Event<?> event);
	void handle(X event);
	
	public static abstract class Default<X extends Event<?>> implements EventHandler<X> {

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
			EventBus.get().registerHandler((EventHandler<Event<?>>) this); 
		}

		
	}
	
	
}