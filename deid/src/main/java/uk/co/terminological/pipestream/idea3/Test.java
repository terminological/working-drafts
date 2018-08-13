package uk.co.terminological.pipestream.idea3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Test {
	
	
	
	
	/*
	public interface EventIntegrator extends EventHandler<Event<?>> {
		public String eventName();
	}
	
	public interface EventIntegratorGenerator extends EventHandlerGenerator<Event<?>> {
		public String eventName();
	}
	*/
	
	public class InputStreamAvailableEvent extends Event.Default<InputStream> {

		DeferredInputStream dis;
		String key;
		
		InputStreamAvailableEvent(DeferredInputStream dis, String key) {
			this.dis = dis;
			this.key = key;
		}
		
		@Override
		public String typeDescription() {
			return key;
		}

		@Override
		public InputStream get() {
			return dis.get();
		}
		
	}
	
	public class DeferredInputStream {
		Path path;
		DeferredInputStream(Path path) {
			this.path = path;
		}
		public InputStream get() {
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				EventBus.get().handleException(e);
				return null;
			}
		}
		public String toString() {return path.toString();}
		public int hashCode() {return path.hashCode();}
	}
	
	public class Reader extends EventGenerator.Default<InputStream> {

		public Reader(Path file, String key) {
			super();
			send(new InputStreamAvailableEvent(new DeferredInputStream(file), key));
		}
		
	}
	
}
