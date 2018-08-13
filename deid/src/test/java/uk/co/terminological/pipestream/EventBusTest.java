package uk.co.terminological.pipestream;

import static org.junit.Assert.*;

import java.util.Optional;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.pipestream.idea3.Event;
import uk.co.terminological.pipestream.idea3.EventBus;
import uk.co.terminological.pipestream.idea3.EventGenerator;
import uk.co.terminological.pipestream.idea3.EventHandler;
import uk.co.terminological.pipestream.idea3.Handlers;
import uk.co.terminological.pipestream.idea3.Metadata;

public class EventBusTest {

	
	public static void main(String[] args) throws Exception {
		EventBusTest tmp = new EventBusTest();
		tmp.setUp();
		tmp.test();
	}
	
	@Before
	public void setUp() throws Exception {

		BasicConfigurator.configure();
		EventBus.get().registerHandler(new TestStringToUpperEventHandler());
		EventBus.get().registerHandler(new TestStringReplaceEventHandler());
		EventBus.get().registerHandler(new TestStringToSystemOutEventHandler());

	}

	@Test
	public final void test() {
		EventBus.get().logInfo("Starting");
		TestGenerator stringLoader = new TestGenerator(); 
		while (stringLoader.execute())
		EventBus.get().logInfo("Finished");
	}


	public static class TestGenerator extends EventGenerator.Default<String> {

		String[] test = {
				"one",
				"two",
				"three",
				"four"
		};

		int i = 0;

		@Override
		public Metadata getMetadata() {
			return Metadata.basic("String message generator");
		}

		@Override
		public Optional<Event<String>> generate() {
			if (i<test.length) return Optional.of(new TestStringEvent(test[i]));
			return Optional.empty();
		}

	}

	public static class TestStringEvent extends Event.Default<String> {

		String message;
		public TestStringEvent(String string) {
			this.message = string;
		}

		@Override
		public String get() {
			return message;
		}

	}

	public static class TestStringNamedEvent extends Event.Default<String> {

		String message;
		EventMetadata<String> metadata;

		public TestStringNamedEvent(String string, String name) {
			this.message = string;
			this.metadata = EventMetadata.named(String.class, name);
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

}