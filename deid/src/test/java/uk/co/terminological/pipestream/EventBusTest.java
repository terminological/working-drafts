package uk.co.terminological.pipestream;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.datatypes.FluentList;
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
			super(FluentEvents.Metadata.forGenerator("STRING_MESSAGE_GENERATOR"));
		}

		String[] test = {
				"one",
				"two",
				"three",
				"four"
		};

		int i = 0;

		@Override
		public List<Event<String>> generate() {
			if (i<test.length) {
				List<Event<String>> out = FluentList.create(new TestStringEvent(test[i]));
				i++;
				return out;
				
			}
			return FluentList.empty();
			
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

	public static class TestStringNamedEvent implements Event<String> {

		String message;
		EventMetadata<String> metadata;
		
		public TestStringNamedEvent(String string, String name) {
			this.metadata = FluentEvents.Metadata.forEvent(String.class, name, "TEST TYPE");
			this.message = string;
		}

		@Override
		public String get() {
			return message;
		}

		@Override
		public EventMetadata<String> getMetadata() {
			return metadata;
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
					FluentEvents.Metadata.forHandler(
							event.getMetadata().name().orElseThrow(() -> new RuntimeException()), 
							"AUTO_BUILD")) {

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
			super(FluentEvents.Metadata.forHandler("collector", "RANDOM_COLLECTOR"));
			this.addDependency("ONE", e -> e instanceof TestStringEvent && e.get().equals("one"));
			this.addDependency("TWO", e -> e.get().equals("ONE"));
		}

		@Override
		public void process() {
			System.out.println("Collected: "+this.getEventByName("ONE").get()+" and "+this.getEventByName("TWO").get());
		}
		
	}

}