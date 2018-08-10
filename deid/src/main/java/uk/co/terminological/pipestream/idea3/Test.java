package uk.co.terminological.pipestream.idea3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Test {
	
	
	public interface Event<Y> extends Cloneable {
		Optional<String> name(Y instance);
		String typeDescription();
		Class<Y> getType();
		Y getCopy();
	}
	
	public interface EventBus {
		void registerHandler(EventHandler<?> handler);
		void receive(Event<?> event);
	}
	
		
	public interface EventHandler<X extends Event<?>> {
		boolean canHandle(X event);
		void handle(X event);
	}
	
	
	public interface EventGenerator<Y> {
		EventBus eventBus();
		void send(Event<Y> event);
	}

	public interface Adaptor<X extends Event<?>,Y> extends EventHandler<X>, EventGenerator<Y> {
		
	}
	
	
	
	
	public class InputStreamAvailableEvent implements Event<InputStream> {

		Path file;
		String key;
		
		InputStreamAvailableEvent(Path file, String key) {
			this.file = file;
		}
		
		@Override
		public Optional<String> name(InputStream instance) {
			return Optional.of(file.toString());
		}

		@Override
		public String typeDescription() {
			return key;
		}

		@Override
		public Class<InputStream> getType() {
			return InputStream.class;
		}

		@Override
		public InputStream getCopy() {
			return Files.newInputStream(file);
		}
		
	}
	
	public class Reader implements EventGenerator<InputStream> {

		EventBus bus;
		public Reader(EventBus bus, Path file, String key) {
			this.bus = bus;
			file.toFile().exists()
			send(new InputStreamAvailableEvent(file, key));
		}
		
		@Override
		public EventBus eventBus() {
			return bus;
		}

		@Override
		public void send(Event<InputStream> event) {
			eventBus().receive(event);
		}
		
	}
	
}
