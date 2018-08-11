package uk.co.terminological.pipestream.idea3;

public interface EventGenerator<Y> extends EventBusAware {
	void send(Event<Y> event);
	
	public static abstract class Default<Y> implements EventGenerator<Y> {

		EventBus bus;
		
		@Override
		public EventBus getEventBus() {
			return bus;
		}

		@Override
		public void setEventBus(EventBus eventBus) {
			this.bus = eventBus;
		}
		
		public Default() {
			this.setEventBus(EventBus.get()); 
		}

		
	}
	
}