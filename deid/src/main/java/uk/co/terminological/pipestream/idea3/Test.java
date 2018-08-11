package uk.co.terminological.pipestream.idea3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Test {
	
	
	public interface Adaptor<X extends Event<?>,Y> extends EventHandler<X>, EventGenerator<Y> {
		
	}
	
	/*
	public interface EventIntegrator extends EventHandler<Event<?>> {
		public String eventName();
	}
	
	public interface EventIntegratorGenerator extends EventHandlerGenerator<Event<?>> {
		public String eventName();
	}
	*/
	
	public class InputStreamAvailableEvent implements Event<InputStream> {

		DeferredInputStream dis;
		String key;
		
		InputStreamAvailableEvent(DeferredInputStream dis, String key) {
			this.dis = dis;
			this.key = key;
		}
		
		@Override
		public Optional<String> name() {
			return Optional.of(dis.toString());
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
			return dis.get();
		}
		
	}
	
	public class DeferredInputStream {
		EventBus bus;
		Path path;
		DeferredInputStream(EventBus bus, Path path) {
			this.bus = bus; this.path = path;
		}
		public InputStream get() {
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				bus.handleException(e);
				return null;
			}
		}
		public String toString() {return path.toString();}
	}
	
	public class Reader implements EventGenerator<InputStream> {

		EventBus bus;
		
		public Reader(EventBus bus, Path file, String key) {
			this.setEventBus(bus);
			
			send(new InputStreamAvailableEvent(new DeferredInputStream(bus,file), key));
		}
		
		@Override
		public void send(Event<InputStream> event) {
			getEventBus().receive(event);
		}

		@Override
		public EventBus getEventBus() {
			return bus;
		}

		@Override
		public void setEventBus(EventBus eventBus) {
			this.bus = eventBus;
		}
		
	}
	
}
