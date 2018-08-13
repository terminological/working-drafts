package uk.co.terminological.pipestream.idea3;

import java.util.Optional;

public interface EventGenerator<Y> extends EventBusAware {
	
	public Metadata getMetadata();
	void send(Event<Y> event, Metadata sender);
	
	public static abstract class Default<Y> implements EventGenerator<Y> {
		
		public void send(Event<Y> event, Metadata sender) {
			getEventBus().receive(event, sender);
		}

		public void send(Event<Y> event) {
			send(event, getMetadata());
		}
		
		public abstract Optional<Event<Y>> generate();
		
		public boolean execute() {
			return generate().map(
					e -> {this.send(e);
						return true;
					}).orElse(false);
		}
		
	}
	
}