package uk.co.terminological.pipestream.idea3;

public interface EventBusAware {
	EventBus getEventBus();
	void setEventBus(EventBus eventBus);
}