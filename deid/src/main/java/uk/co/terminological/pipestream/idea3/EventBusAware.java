package uk.co.terminological.pipestream.idea3;

public interface EventBusAware {
	default EventBus getEventBus() { return EventBus.get(); }
}