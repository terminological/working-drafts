package uk.co.terminological.pipestream;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import uk.co.terminological.datatypes.FluentMap;

/**
 * Event handlers do a range of tasks such as:
 * <li> {@link Adaptor}: Convert data from one type to another and resubmit to event bus
 * <li> {@link Processor}: Generically process the body of the event (excluding metadata)
 * <li> {@link EventProcessor}: Generically process the whole event (including metadata) 
 * <li> {@link Terminal}: Consume data from the body of the event without returning data 
 * <li> {@link Collector}: Collect data from many events until a set of criteria are met
 *  
 * @author robchallen
 *
 */
public class HandlerTypes {

	/**
	 * Convert data from one type to another and resubmit to event bus. 
	 * Subclasses must implement the {@link Adaptor.convert} method, or a fluent constructor
	 * using functional interfaces available in the {@link FluentEvents.Handlers.adaptor} methods
	 * @author robchallen
	 *
	 * @param <X> input data type
	 * @param <Y> output data type
	 */
	public abstract static class Adaptor<X,Y> extends EventHandler.Default<Event<X>> {

		public Adaptor(String type) {
			super(FluentEvents.Metadata.forHandler(type));
		}
		
		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			Event<Y> event2 = convert(event.get());
			this.send(event2);
		}
		
		public abstract Event<Y> convert(X input);

	}
	
	/**
	 * Generically process the body of the event (excluding metadata). Subclasses must implement the process method, or use the
	 * {@link FluentEvents.Handlers.process} fluent / functional interface. 
	 * @author robchallen
	 *
	 * @param <X> the input type
	 */
	public abstract static class Processor<X> extends EventHandler.Default<Event<X>>  {

		public Processor(String type) {
			super(FluentEvents.Metadata.forHandler(type));
		}
		
		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			process(event.get(), this);
		}
		
		public abstract void process(X x, Processor<X> context);


	}
	
	public abstract static class EventProcessor<X> extends EventHandler.Default<Event<X>>  {

		public EventProcessor(String type) {
			super(FluentEvents.Metadata.forHandler(type));
		}
		
		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			process(event, this);
		}
		
		public abstract void process(Event<X> event, EventProcessor<X> context);


	}
	
	public abstract static class Terminal<X> extends EventHandler.Default<Event<X>>  {

		public Terminal(String type) {
			super(FluentEvents.Metadata.forHandler(type));
		}
		
		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			consume(event.get());
		}
		
		public abstract void consume(X x);


	}
	
	public static class PredicateMap 
		extends FluentMap<String,Predicate<Event<?>>>
		implements Map<String,Predicate<Event<?>>> {} 
	
	
	
	public abstract static class Collector implements EventHandler<Event<?>> {
		
		Map<String,Predicate<Event<?>>> tests = new HashMap<>(); 
		Map<String,Event<?>> dependencies = new HashMap<>();
		HandlerMetadata metadata;
		
		/**
		 * extending this constructor to include many
		 * 
		 * addDependency(name, Predicate<Event>) stanzas
		 * @param metadata
		 */
		public Collector(HandlerMetadata metadata, Map<String,Predicate<Event<?>>> tests) {
			this.metadata = metadata;
			this.tests = tests;
		}
		
		@Override
		public boolean canHandle(Event<?> event) {
			for (Map.Entry<String,Predicate<Event<?>>> test : tests.entrySet()) {
				
				if (test.getValue().test(event)) {
					if (!dependencies.containsKey(test.getKey())) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void handle(Event<?> event) {
			
			for (Map.Entry<String,Predicate<Event<?>>> test : tests.entrySet()) {
				if (test.getValue().test(event)) {
					if (!dependencies.containsKey(test.getKey())) {
						dependencies.put(test.getKey(), event);
					}
				}
			}
			
			if (dependenciesMet()) {
				process();
			}
			
		}
		
		public Event<?> getEventByName(String name) {
			return dependencies.get(name);
		}
		
		/**
		 * Override this
		 * 
		 * Use getEventByName(String name) to get the various events
		 * and send(Event<?> event) to output onto the message bus
		 * if required 
		 */
		public abstract void process();
		
		public void send(Event<?> event) {
			getEventBus().receive(event, getMetadata());
		}
		
		public boolean dependenciesMet() {
			if (tests.isEmpty()) return false;
			return tests.keySet().containsAll(dependencies.keySet()) &&
					dependencies.keySet().containsAll(tests.keySet());
		}
		
		@Override
		public HandlerMetadata getMetadata() {
			return metadata;
		}
		
	}
	
	public abstract class CollectorGenerator implements EventHandlerGenerator<Event<?>> {

		Map<String,Predicate<Event<?>>> tests = new HashMap<>(); 
		
		public CollectorGenerator(Map<String,Predicate<Event<?>>> predicateMap) {
			this.tests = predicateMap;
		}
		
		@Override
		public boolean canCreateHandler(Event<?> event) {
			return tests.values().stream().anyMatch(p -> p.test(event));
		}

		/**
		 * Utility for creating and additional rule that must apply to all inputs of 
		 * collector. 
		 * @param additionalTest
		 * @return
		 */
		public Map<String,Predicate<Event<?>>> instanceTests(Predicate<Event<?>> additionalTest) {
			Map<String,Predicate<Event<?>>> out = new HashMap<String,Predicate<Event<?>>>();
			for (Entry<String,Predicate<Event<?>>> old: tests.entrySet()) {
				out.put(old.getKey(), 
						old.getValue().and(additionalTest)
						);
			}
			return out;
		}
		
		@Override
		public abstract Collector createHandlerFor(Event<?> event);
		
	}
	
	
}
