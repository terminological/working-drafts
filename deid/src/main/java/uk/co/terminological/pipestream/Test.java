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

import uk.co.terminological.datatypes.FluentList;

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
		public List<Event<InputStream>> generate() {
			return FluentList.create(out);
		}
		
	}
	
	
	public class FileChangedEvent extends Event.Default<Path> {

		String key;
		Path path;
		
		FileChangedEvent(Path path, String fileType) {
			
			super(EventMetadata.named(Path.class, fileType));
			this.path = path;
		}
		
	}
	
	
	public class Watcher extends EventGenerator.Default<Path> {

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
			key = watcher.poll();
			key.pollEvents().stream().map(we -> (Path) we.context());
			return Optional.ofNullable(key).map(wk -> wk.);
		}
		
	}
	
}
