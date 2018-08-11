package uk.co.terminological.pipestream.idea3;

public interface EventHandler<X extends Event<?>> extends EventBusAware {
	boolean canHandle(Event<?> event);
	void handle(X event);
}