package uk.co.terminological.pipestream;

import static org.junit.Assert.*;

import java.util.Optional;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.EventHandler.HandlerMetadata;

public class EventBusTest {

	EventBus bus;
	
	public static void main(String[] args) throws Exception {
		EventBusTest tmp = new EventBusTest();
		tmp.setUp();
		tmp.test();
	}
	
	@Before
	public void setUp() throws Exception {

		BasicConfigurator.configure();
		bus = EventBus.get()
				.withHandler(new TestStringToUpperEventHandler())
				.withHandler(new TestStringReplaceEventHandler())
				.withHandler(new TestStringToSystemOutEventHandler())
				.withHandlerGenerator(new TestStringEventHandlerGenerator())
				.withHandler(new TestStringCollector());
	}

	@Test
	public final void test() {
		EventBus.get().logInfo("Starting");
		TestGenerator stringLoader = new TestGenerator(); 
		
		while(stringLoader.execute());
		
		EventBus.get().logInfo("Finished");
	}


	public static class TestGenerator extends EventGenerator.Default<String> {

		public TestGenerator() {
			super(Metadata.basic("String message generator"));
		}

		String[] test = {
				"one",
				"two",
				"three",
				"four"
		};

		int i = 0;

		@Override
		public Optional<Event<String>> generate() {
			if (i<test.length) {
				
				Optional<Event<String>> out = Optional.of(new TestStringEvent(test[i]));
				i++;
				return out;
				
			}
			return Optional.empty();
			
		}

	}

	public static class TestStringEvent extends Event.Default<String> {

		String message;
		public TestStringEvent(String string) {
			super(string);
			this.message = string;
		}

		@Override
		public String get() {
			return message;
		}

	}

	public static class TestStringNamedEvent extends Event.Default<String> {

		String message;
		
		public TestStringNamedEvent(String string, String name) {
			super(EventMetadata.named(String.class, name));
			this.message = string;
		}

		@Override
		public String get() {
			return message;
		}

	}

	public static class TestStringToUpperEventHandler extends Handlers.Adaptor<String, String> {
		@Override
		public boolean canHandle(Event<?> event) {
			return (event instanceof TestStringEvent);
		}

		@Override
		public Event<String> convert(String input) {
			this.getEventBus().logInfo("Converting to upper case: "+input);
			return new TestStringNamedEvent(input.toUpperCase(), "Upper case");
		}
	}

	public static class TestStringReplaceEventHandler extends Handlers.Adaptor<String, String> {
		@Override
		public boolean canHandle(Event<?> event) {
			return (event instanceof TestStringNamedEvent
					&& event.getMetadata().name().orElse("").equals("Upper case")
					);
		}

		@Override
		public Event<String> convert(String input) {
			this.getEventBus().logInfo("Replacing: "+input);
			return new TestStringNamedEvent(input.replace("O", "XXX"), "Replaced");
		}
	}

	public static class TestStringToSystemOutEventHandler extends EventHandler.Default<Event<String>> {

		@Override
		public boolean canHandle(Event<?> event) {
			return String.class.isAssignableFrom(event.getMetadata().getType());
		}

		@Override
		public void handle(Event<String> event) {
			this.getEventBus().logInfo("Writing...");
			System.out.println(event.get());
		}
		
	}
	
	public static class TestStringEventHandlerGenerator extends EventHandlerGenerator.Default<Event<String>> {

		@Override
		public boolean canCreateHandler(Event<?> event) {
			return String.class.isAssignableFrom(event.getMetadata().getType());
		}

		@Override
		public EventHandler<Event<String>> createHandlerFor(Event<String> event) {
			return new EventHandler.Default<Event<String>>(
					HandlerMetadata.named(event.getMetadata().name().orElseThrow(() -> new RuntimeException()), 
							"Auto build")) {

				@Override
				public boolean canHandle(Event<?> event2) {
					return TestStringEventHandlerGenerator.this.canCreateHandler(event2) &&
							event2.getMetadata().name().equals(this.getMetadata().name())
							;
				}

				@Override
				public void handle(Event<String> event2) {
					this.getEventBus().logInfo("Generated handler: "+this.getMetadata()+" operating on "+event2.getMetadata());
					System.out.println("Generated:"+event.get());
				}

				
			};
		}
		
	}
	
	public static class TestStringCollector extends Handlers.Collector {
		
		public TestStringCollector() {
			super(HandlerMetadata.named("collector", "random"));
			this.addDependency("ONE", e -> e instanceof TestStringEvent && e.get().equals("one"));
			this.addDependency("TWO", e -> e instanceof TestStringNamedEvent && e.get().equals("ONE"));
		}

		@Override
		public void process() {
			System.out.println("Collected: "+this.getEventByName("ONE").get()+" and "+this.getEventByName("TWO").get());
		}
		
	}

}