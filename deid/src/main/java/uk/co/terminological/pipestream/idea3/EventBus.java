package uk.co.terminological.pipestream.idea3;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus {
	
	static EventBus singleton;
	public static EventBus get() {
		if (singleton == null) singleton = new EventBus();
		return singleton;
	}
	
	List<EventHandler<?>> handlers = new ArrayList<>();
	List<EventHandlerGenerator<?>> handlerGenerators = new ArrayList<>();;
	
	Logger log = LoggerFactory.getLogger(EventBus.class);
	
	
	void registerHandlerGenerator(EventHandlerGenerator<?> handlerGenerator) {
		handlerGenerators.add(handlerGenerator);
	};
	void registerHandler(EventHandler<?> handler) {
		handlers.add(handler);
	};
	
	<X extends Event<?>> void receive(X event) {
		//TODO do something in parallel here unsing ? fibers
		// https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md
		// http://www.paralleluniverse.co/quasar/
		handlers.parallelStream().filter(h -> h.canHandle(event)).forEach(h -> h.handle(event));
	};
	
	
	void handleException(Exception e) {
		PrintStream ps = new PrintStream(new ByteArrayOutputStream());
		e.printStackTrace(ps);
		logError(ps.toString());
	};
	
	
	void logError(String message) {
		log.error(message);
	};
	void logInfo(String message) {
		log.info(message);
	};
	
	List<EventHandler<?>> getHandlers() {return handlers;};
	List<EventHandlerGenerator<?>> getHandlerGenerators() {return handlerGenerators;}
	
	void releaseHandler(EventHandler<?> handler) {
		handlers.remove(handler);
	};
}