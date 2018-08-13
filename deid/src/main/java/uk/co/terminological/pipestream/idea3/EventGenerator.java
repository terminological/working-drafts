package uk.co.terminological.pipestream.idea3;

public interface EventGenerator<Y> extends EventBusAware {
	
	public Metadata getMetadata();
	void send(Event<Y> event, Metadata sender);
	
	public static abstract class Default<Y> implements EventGenerator<Y> {
		
		public void send(Event<Y> event) {
			getEventBus().receive(event, getMetadata());
		}

		
	}
	
}