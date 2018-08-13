package uk.co.terminological.pipestream;

public interface EventBusAware {
	default EventBus getEventBus() { return EventBus.get(); }
}