package uk.co.terminological.pipestream.idea3;

import java.util.List;

public interface EventBus {
	void registerHandlerGenerator(EventHandlerGenerator<?> handler);
	void registerHandler(EventHandler<?> handler);
	void receive(Event<?> event);
	
	void handleException(Exception e);
	void logError(String message);
	void logInfo(String message);
	
	List<EventHandler<?>> getHandlers();
	List<EventHandlerGenerator<?>> getHandlerGenerators();
	
	void releaseHandler(EventHandler<?> handler);
}