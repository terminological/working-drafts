package uk.co.terminological.pipestream;

import java.io.IOException;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import uk.co.terminological.datatypes.FluentList;

public class Test {
	
	
	
	
	
	
	public static class InputStreamAvailableEvent implements Event<InputStream> {

		DeferredInputStream dis;
		EventMetadata<InputStream> metadata;
		
		InputStreamAvailableEvent(DeferredInputStream dis, String key) {
			metadata = EventMetadata.named(InputStream.class, key);
			this.dis = dis;
		}
		
		@Override
		public InputStream get() {
			return dis.get();
		}

		@Override
		public EventMetadata<InputStream> getMetadata() {
			return metadata;
		}
		
	}
	
	public static  class DeferredInputStream {
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
	
	
	public static  class Reader extends EventGenerator.Default<InputStream> {

		InputStreamAvailableEvent out;
		
		public Reader(Path file, String key) {
			super(Metadata.named(file.toString(),"File reader"));
			out = new InputStreamAvailableEvent(new DeferredInputStream(file), key);
		}

		@Override
		public List<Event<InputStream>> generate() {
			return FluentList.create(out);
		}
		
	}
	
	
	public static  class FileChangedEvent extends Event.Default<Path> {
		
		FileChangedEvent(Path path) {
				super(EventMetadata.named(Path.class, type(path) ),path);
		}
		
		static String type(Path p) {
			try {
				return Files.probeContentType(p);
			} catch (IOException e) {
				return "unknown";
			}
		}
	}
	
	
	public static class Watcher extends EventGenerator.Default<Path> {

		WatchService watcher;
		WatchKey key;
		
		public Watcher(Path dir, String name) {
			super(Metadata.named(dir.toString(),name));
			
			
			try {
				watcher = FileSystems.getDefault().newWatchService();
			    key = dir.register(watcher,
			                           ENTRY_CREATE,
			                           ENTRY_MODIFY);
			    
			} catch (IOException x) {
			    this.getEventBus().handleException(x);
			}
		}

		@Override
		public List<Event<Path>> generate() {
			return watcher.poll().pollEvents().stream().map(we -> (Path) we.context())
				.map(p -> new FileChangedEvent(p))
				.collect(Collectors.toList());
			
		}
		
	}
	
}
