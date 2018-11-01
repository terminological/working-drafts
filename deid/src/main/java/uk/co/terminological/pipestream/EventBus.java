package uk.co.terminological.pipestream;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.TupleList;
import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.EventHandler.HandlerMetadata;

//TODO: Discuss whether worth writing this up as a architecture for data processing.
//TODO: better error handling, e.g. retry on error.
//TODO: file watcher that reports changes since last processing time.
//TODO: some form of execution success status reported to the bus from the handlers
//TODO: execution failure (exception) report. auto serialise and replay messages that trigger failures.
//TODO: Custom logging factory: http://poth-chola.blogspot.com/2015/08/custom-slf4j-logger-adapter.html

public class EventBus {       

	//Thanks to: https://stackoverflow.com/questions/16106260/thread-safe-singleton-class/16106598#16106598
    private static class Holder {
       private static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus get() {
        return Holder.INSTANCE;
    }
	
	TupleList<Metadata, EventMetadata<?>> eventHistory = TupleList.create();
	TupleList<EventMetadata<?>,HandlerMetadata> processingHistory = TupleList.create();
	
	List<Event<?>> unhandled = new ArrayList<>();
	List<EventGenerator<?>> generators = new ArrayList<>();
	List<EventHandler<Event<?>>> handlers = new ArrayList<>();
	List<EventHandlerGenerator<Event<?>>> handlerGenerators = new ArrayList<>();
	List<Closeable> openResources = new ArrayList<>();
	
	Map<Class<?>,Object> apis = new HashMap<Class<?>, Object>();
	
	boolean rethrowErrors = false;
	
	public EventBus withApi(Object api) {
		this.apis.put(api.getClass(),api);
		log.info("Added api: "+api.getClass().getCanonicalName());
		return this;
	}
	
	public EventBus debugMode() {
		rethrowErrors = true;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <X> Optional<X> getApi(Class<X> apiClass) {
		return Optional.ofNullable((X) apis.get(apiClass));
	}
	
	Logger log = LoggerFactory.getLogger(EventBus.class);
	
	
	@SuppressWarnings("unchecked")
	public EventBus withHandlerGenerator(EventHandlerGenerator<? extends Event<?>> handlerGenerator) {
		handlerGenerators.add((EventHandlerGenerator<Event<?>>) handlerGenerator);
		log.info("Added handler generator: "+handlerGenerator.getClass().getCanonicalName());
		return this;
	};
	
	public EventBus withEventGenerator(EventGenerator<?> eventGenerator) {
		generators.add(eventGenerator);
		log.info("Added event generator: "+eventGenerator.getMetadata().toString());
		return this;
	};
	
	@SuppressWarnings("unchecked")
	public EventBus withHandler(EventHandler<? extends Event<?>> handler) {
		handlers.add((EventHandler<Event<?>>) handler);
		log.info("Added event handler: "+handler.getMetadata().toString());
		return this;
	};
	
	void receive(Event<?> event, Metadata metadata) {
		log.debug("Recieved message: {}",event.getMetadata());
		//TODO do something in parallel here using ? fibers
		// https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md
		// http://www.paralleluniverse.co/quasar/
		this.eventHistory.and(metadata, event.getMetadata());
		if (event.getMetadata().reusable()) {
			handlers.parallelStream().filter(h -> h.canHandle(event)).forEach(
					h -> {
						log.debug("handling message: {} with handler: {}",event.getMetadata(), h.getMetadata());
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
					}
			);
			handlerGenerators.parallelStream().filter(hg -> hg.canCreateHandler(event)).forEach(
					hg -> {
						EventHandler<Event<?>> h = hg.createHandlerFor(event);
						log.debug("handling message: {} with handler: {}",event.getMetadata(), h.getMetadata());
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
					}
					);
		} else {
			handlers.stream().filter(h -> h.canHandle(event)).findFirst().ifPresentOrElse(
					(h -> {
						log.debug("handling message: {} with handler: {}",event.getMetadata(), h.getMetadata());
						processingHistory.and(event.getMetadata(),h.getMetadata());
						h.handle(event);
						this.releaseHandler(h);
					}),
					(() -> {
						handlerGenerators.stream().filter(hg -> hg.canCreateHandler(event))
							.findFirst().ifPresentOrElse(
							(hg -> {
								EventHandler<Event<?>> h = hg.createHandlerFor(event);
								log.debug("handling message: {} with handler: {}",event.getMetadata(), h.getMetadata());
								processingHistory.and(event.getMetadata(),h.getMetadata());
								h.handle(event);
								this.releaseHandler(h);
							}),
							(() -> {
								log.debug("unhandled message: {}",event.getMetadata());
								unhandled.add(event);
							})
							);
					})
			);
		}
		//TODO some process for dealing with unhandled events
		// a default handler?
		// a logging process?
		
	};
	
	public void handleException(Exception e) {
		//TODO: Some sort of error handling policy
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		log.error(baos.toString());
		if (this.rethrowErrors) throw new RuntimeException(e);
	};
	
	
	public void logError(String message) {
		log.error(message);
	};
	public void logInfo(String message) {
		log.info(message);
	};
	
	List<EventHandler<Event<?>>> getHandlers() {return handlers;};
	List<EventHandlerGenerator<Event<?>>> getHandlerGenerators() {return handlerGenerators;}
	
	void releaseHandler(EventHandler<?> handler) {
		handlers.remove(handler);
	}

	public void registerCloseable(Closeable closeable) {
		this.openResources.add(closeable);
	};
	
	//TODO: so this fires the whole graph execution but we would like to be able to run this as a daemon
	//maybe use quartz for this.
	public EventBus execute() {
		log.info("Starting eventBus generators");
		generators.parallelStream().forEach(g -> {while(g.execute()) {};});
		return this;
	}
	
	public EventBus sendShutdownMessage() {
		log.info("Starting eventBus generators");
		this.receive(new Event.Shutdown(), FluentEvents.Metadata.forGenerator("SYSTEM"));
		return this;
	}
	
	public EventBus writeExecutionGraphs(Path directory) throws IOException {
		ExecutionHistoryUtils e = new ExecutionHistoryUtils(directory);
		//TODO: refine small dot graph in terms of different format for events versus handlers & SVG output
		//TODO: freemind file for flat tsv export of data or json data for vis.js.
		// e.generate(ExecutionHistoryUtils.BIG_DOT_GRAPH, directory.resolve("large.dot"), "1");
		e.generate(ExecutionHistoryUtils.SMALL_DOT_GRAPH, directory.resolve("small.dot"), "2");
		e.executeGraphviz(directory.resolve("small.dot"));
		return this;
	}
	
	public void shutdown() {
		for (Closeable toClose: openResources) {
			try {
				toClose.close();
			} catch (IOException e) {
				this.logError(e.getMessage());
				//We tried
			}
		}
	}
	
	protected TupleList<Metadata, EventMetadata<?>> getEventHistory() {return eventHistory;}
	protected TupleList<EventMetadata<?>,HandlerMetadata> getProcessingHistory() {return processingHistory;}
}