package uk.co.terminological.pipestream.idea3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Test {
	
	
	public interface EventBus {
		void registerHandlerGenerator(EventHandlerGenerator<?> handler);
		void registerHandler(EventHandler<?> handler);
		void receive(Event<?> event);
		
		void handleException(Exception e);
		void logError(String message);
		void logInfo(String message);
		
		List<EventHandler<?>> getHandlers();
		List<EventHandlerGenerator<?>> getHandlerGenerators();
		
		void releaseHandler(EventHandler<?> handler);
	}
	
	public interface EventBusAware {
		EventBus getEventBus();
		void setEventBus(EventBus eventBus);
	}
		
	public interface EventHandler<X extends Event<?>> extends EventBusAware {
		boolean canHandle(X event);
		void handle(X event);
	}
	
	public interface EventHandlerGenerator<X extends Event<?>> extends EventBusAware {
		boolean canCreateHandler(X event);
		EventHandler<X> createHandlerAndHandle(X event);
	}
	
	public interface EventGenerator<Y> extends EventBusAware {
		void send(Event<Y> event);
	}

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
