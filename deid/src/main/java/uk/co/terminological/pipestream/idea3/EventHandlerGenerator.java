package uk.co.terminological.pipestream.idea3;

public interface EventHandlerGenerator<X extends Event<?>> extends EventBusAware {
	boolean canCreateHandler(X event);
	EventHandler<X> createHandlerAndHandle(X event);
}