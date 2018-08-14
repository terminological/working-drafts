package uk.co.terminological.pipestream;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public abstract class EventSerializer<X> implements Closeable {
	public EventSerializer() {
		if (leaveOpen()) EventBus.get().registerCloseable(this);
	}
	public abstract void ensureOpen(Path path);
	public abstract boolean leaveOpen();
	public abstract void close() throws IOException;
	public abstract void write(Event<X> u);
	
	
	
	public static class JavaSerializer extends EventSerializer<Serializable> {

		BufferedWriter os;
		
		@Override
		public void ensureOpen(Path path) {
			os = Files.newBufferedWriter(path);
		}

		@Override
		public boolean leaveOpen() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void write(Event<Serializable> u) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}