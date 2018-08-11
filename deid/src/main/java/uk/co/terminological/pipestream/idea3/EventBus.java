package uk.co.terminological.pipestream.idea3;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus {       

    private static class Holder {
       private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus get() {
        return Holder.INSTANCE;
    }
	
	List<Event<?>> history = new ArrayList<>();
	List<EventHandler<Event<?>>> handlers = new ArrayList<>();
	List<EventHandlerGenerator<Event<?>>> handlerGenerators = new ArrayList<>();;
	
	Logger log = LoggerFactory.getLogger(EventBus.class);
	
	
	void registerHandlerGenerator(EventHandlerGenerator<Event<?>> handlerGenerator) {
		handlerGenerators.add(handlerGenerator);
		//handlerGenerator.setEventBus(this);
	};
	
	public void registerHandler(EventHandler<Event<?>> handler) {
		handlers.add(handler);
		//handler.setEventBus(this);
	};
	
	void receive(Event<?> event) {
		//TODO do something in parallel here unsing ? fibers
		// https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md
		// http://www.paralleluniverse.co/quasar/
		this.history.add(event);
		if (event.multiProcess()) {
			handlers.parallelStream().filter(h -> h.canHandle(event)).forEach(h -> h.handle(event));
			handlerGenerators.parallelStream().filter(hg -> hg.canCreateHandler(event)).forEach(
					hg -> hg.createHandlerAndHandle(event)
					);
		} else {
			handlers.stream().filter(h -> h.canHandle(event)).findFirst().ifPresentOrElse(
					h -> h.handle(event),
					() -> handlerGenerators.stream().filter(hg -> hg.canCreateHandler(event)).findFirst().ifPresent(
							hg -> hg.createHandlerAndHandle(event))
					);
		}
		
		
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
	
	List<EventHandler<Event<?>>> getHandlers() {return handlers;};
	List<EventHandlerGenerator<Event<?>>> getHandlerGenerators() {return handlerGenerators;}
	
	void releaseHandler(EventHandler<?> handler) {
		handlers.remove(handler);
	};
}