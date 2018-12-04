package uk.co.terminological.pipestream;

/**
 * Marks classes that access the {@link EventBus} directly
 * @author robchallen
 *
 */
public interface EventBusAware {

	default EventBus getEventBus() { return EventBus.get(); }

}