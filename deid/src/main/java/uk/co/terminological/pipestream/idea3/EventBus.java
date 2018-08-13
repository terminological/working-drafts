package uk.co.terminological.pipestream.idea3;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.TupleList;
import uk.co.terminological.pipestream.idea3.Event.EventMetadata;
import uk.co.terminological.pipestream.idea3.EventHandler.HandlerMetadata;

public class EventBus {       

	//Thanks to: https://stackoverflow.com/questions/16106260/thread-safe-singleton-class/16106598#16106598
    private static class Holder {
       private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus get() {
        return Holder.INSTANCE;
    }
	
	List<EventMetadata<?>> eventHistory = new ArrayList<>();
	TupleList<EventMetadata<?>,HandlerMetadata> processingHistory = TupleList.create();
	
	List<Event<?>> unhandled = new ArrayList<>();
	List<EventHandler<Event<?>>> handlers = new ArrayList<>();
	List<EventHandlerGenerator<Event<?>>> handlerGenerators = new ArrayList<>();
	
	Map<Class<?>,Object> apis = new HashMap<Class<?>, Object>();
	
	public void registgerApi(Object api) {
		this.apis.put(api.getClass(),api);
	}
	
	@SuppressWarnings("unchecked")
	public <X> Optional<X> getApi(Class<X> apiClass) {
		return Optional.ofNullable((X) apis.get(apiClass));
	}
	
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
		//TODO do something in parallel here using ? fibers
		// https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md
		// http://www.paralleluniverse.co/quasar/
		this.eventHistory.add(event.getMetadata());
		if (event.getMetadata().reusable()) {
			handlers.parallelStream().filter(h -> h.canHandle(event)).forEach(
					h -> {
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
					}
			);
			handlerGenerators.parallelStream().filter(hg -> hg.canCreateHandler(event)).forEach(
					hg -> {
						EventHandler<Event<?>> h = hg.createHandlerFor(event);
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
					}
					);
		} else {
			handlers.stream().filter(h -> h.canHandle(event)).findFirst().ifPresentOrElse(
					(h -> {
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
						this.releaseHandler(h);
					}),
					(() -> {
						handlerGenerators.stream().filter(hg -> hg.canCreateHandler(event))
							.findFirst().ifPresentOrElse(
							(hg -> {
								EventHandler<Event<?>> h = hg.createHandlerFor(event);
								processingHistory.and(event.getMetadata(),h.getMetadata());
								h.handle(event);
								this.releaseHandler(h);
							}),
							(() -> unhandled.add(event))
							);
					})
			);
		}
		//TODO some process for dealing with unhandled events
		
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