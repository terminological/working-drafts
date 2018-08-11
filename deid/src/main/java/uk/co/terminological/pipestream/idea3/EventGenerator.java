package uk.co.terminological.pipestream.idea3;

public interface EventGenerator<Y> extends EventBusAware {
	void send(Event<Y> event);
	
	public static abstract class Default<Y> implements EventGenerator<Y> {

		
		
		
		public void send(Event<Y> event) {
			getEventBus().receive(event);
		}

		
	}
	
}