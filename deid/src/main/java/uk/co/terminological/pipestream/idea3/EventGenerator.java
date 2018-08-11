package uk.co.terminological.pipestream.idea3;

public interface EventGenerator<Y> extends EventBusAware {
	void send(Event<Y> event);
}