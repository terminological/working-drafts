package uk.co.terminological.pipestream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Test {
	
	
	
	
	
	
	public class InputStreamAvailableEvent extends Event.Default<InputStream> {

		DeferredInputStream dis;
		String key;
		
		InputStreamAvailableEvent(DeferredInputStream dis, String key) {
			super(EventMetadata.named(InputStream.class, key));
			this.dis = dis;
			this.key = key;
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

		InputStreamAvailableEvent out;
		
		public Reader(Path file, String key) {
			super(Metadata.named(file.toString(),"File reader"));
			out = new InputStreamAvailableEvent(new DeferredInputStream(file), key);
		}

		@Override
		public Optional<Event<InputStream>> generate() {
			return Optional.of(out);
		}
		
	}
	
}
