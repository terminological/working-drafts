package uk.co.terminological.pipestream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.StreamExceptions.FunctionWithException;

public class FileUtils {
	
	public static class InputStreamAvailableEvent implements Event<InputStream> {

		DeferredInputStream<?> dis;
		EventMetadata<InputStream> metadata;
		
		InputStreamAvailableEvent(DeferredInputStream<?> dis, String key) {
			metadata = FluentEvents.Metadata.forEvent(InputStream.class, key);
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

		@Override
		public Event<InputStream> put(String key, Object o) {
			return this;
		}

		@Override
		public Object get(String key) {
			return null;
		}
		
	}
	
	public abstract static class DeferredInputStream<X extends InputStream> {
		Path path;
		DeferredInputStream(Path path) {
			this.path = path;
		}
		public X get() {
			try {
				return pathToStream(path);
			} catch (Exception e) {
				EventBus.get().handleException(e);
				return null;
			}
		}
		
		public abstract X pathToStream(Path path) throws Exception;
		
		public String toString() {return path.toString();}
		public int hashCode() {return path.hashCode();}
		
		public static <Y extends InputStream> DeferredInputStream<Y> create(Path path, FunctionWithException<Path,Y,Exception> generator) {
			return new DeferredInputStream<Y>(path) {
				@Override
				public Y pathToStream(Path path) throws Exception {
					return generator.apply(path);
				}
			};
		}
	}
	
	
	public static  class Reader extends EventGenerator.Default<InputStream> {

		InputStreamAvailableEvent out;
		
		public Reader(Path file, String key) {
			super(FluentEvents.Metadata.forGenerator(file.toString(),"FILE_READER"));
			
			out = new InputStreamAvailableEvent(
					DeferredInputStream.create(file, path -> Files.newInputStream(file)), key);
		}

		@Override
		public List<Event<InputStream>> generate() {
			return FluentList.create(out);
		}
		
	}
	
	
	public static class FileChangedEvent extends Event.Default<Path> {
		FileChangedEvent(Path path) {
				super(FluentEvents.Metadata.forEvent(path, 
						p -> p.toString(),
						p -> type(p)),path);
		}
	}
	
	static String type(Path p) {
		try {
			return Files.probeContentType(p);
		} catch (IOException e) {
			return "unknown";
		}
	}
	
	//TODO: create test cases
	public static class Watcher extends EventGenerator.Default<Path> {

		WatchService watcher;
		WatchKey key;
		
		public Watcher(Path dir) {
			super(FluentEvents.Metadata.forGenerator(dir.toString(),"DIRECTORY_WATCHER"));
			
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
			return watcher.poll().pollEvents()
					.stream().map(we -> (Path) we.context())
					.map(p -> new FileChangedEvent(p))
					.collect(Collectors.toList());
			
		}
	}
	
	//TODO: check currency of files - files changed after certain date. File path filter.
	
	public static class DirectoryScanner extends EventGenerator.Default<Path> {

		private Path path;
		private FileFilter filter;
		Function<Path,String> nameGenerator;
		Function<Path,String> typeGenerator;

		public DirectoryScanner(Path directory, FileFilter filter,
				Function<Path,String> nameGenerator,
				Function<Path,String> typeGenerator
				) {
			super(FluentEvents.Metadata.forGenerator(directory.toString(), "DIRECTORY_SCANNER"));
			this.path = directory;
			this.filter = filter;
			this.nameGenerator = (nameGenerator == null ? (path -> path.toString()): nameGenerator);
			this.typeGenerator = (typeGenerator == null ? (path -> type(path)): typeGenerator);
		}

		@Override
		public List<Event<Path>> generate() {
			return Arrays.asList(path.toFile().listFiles(filter)).stream().map(
					file -> {
						Path tmp = file.toPath(); 
						return FluentEvents.Events.namedTypedEvent(
							tmp, 
							path -> path.toString(), 
							path -> type(path));
					}
			).collect(Collectors.toList());
		}
		
	}
	
	public static interface NamingStrategy extends Function<Event<?>,Path> {}
	
	public static class FileWriter<X> extends EventHandler.Default<Event<X>> {

		NamingStrategy nameStrategy; 
		EventSerializer<X> serialiser;
		Predicate<Event<?>> acceptEvents;
		
		public FileWriter(Predicate<Event<?>> acceptEvents, NamingStrategy nameStrategy, EventSerializer<X> serialiser) {
			super(FluentEvents.Metadata.forHandler(serialiser.getClass().getSimpleName()));
			this.nameStrategy = nameStrategy;
			this.serialiser = serialiser;
			this.acceptEvents = acceptEvents;
		}
		
		@Override
		public boolean canHandle(Event<?> event) {
			return acceptEvents.test(event);
		}

		@Override
		public void handle(Event<X> event) {
			Path out = nameStrategy.apply(event);
			this.getEventBus().logInfo("Started writing "+event.getMetadata().toString()+" to "+out.toString());
			serialiser.write(event);
			this.getEventBus().logInfo("Finished writing "+event.getMetadata().toString()+" to "+out.toString());
			
		}
		
	}
	
}
