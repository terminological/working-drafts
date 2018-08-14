package uk.co.terminological.pipestream;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Handlers {

	
	public abstract static class Adaptor<X,Y> extends EventHandler.Default<Event<X>> implements EventGenerator<Y>  {

		public Adaptor() {
			super(FluentEvents.Metadata.forHandler("ADAPTOR"));
		}
		
		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			send(convert(event.get()));
		}
		
		public abstract Event<Y> convert(X input);

		@Override
		public void send(Event<Y> event) {
			getEventBus().receive(event, getMetadata());
		}

		
		
	}
	
	
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
		public Collector(HandlerMetadata metadata) {
			this.metadata = metadata;
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
		
		/**
		 * define dependencies in constructor
		 * @param name
		 * @param test
		 */
		public void addDependency(String name, Predicate<Event<?>> test) {
			if (tests.containsKey(name)) throw new UnsupportedOperationException("Name "+name+" already present as dependency");
			this.tests.put(name, (Predicate<Event<?>>) test);
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
	
	/*
	public interface EventIntegrator extends EventHandler<Event<?>> {
		public String eventName();
	}
	
	public interface EventIntegratorGenerator extends EventHandlerGenerator<Event<?>> {
		public String eventName();
	}
	*/
	
}
