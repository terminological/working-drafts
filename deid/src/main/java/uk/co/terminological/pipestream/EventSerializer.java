package uk.co.terminological.pipestream;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class EventSerializer<X> implements Closeable {
	public EventSerializer() {
		if (leaveOpen()) EventBus.get().registerCloseable(this);
	}
	public abstract void ensureOpen(Path path);
	public abstract boolean leaveOpen();
	public abstract void close() throws IOException;
	public abstract void write(Event<X> u);
	
	
	
	public static class JavaSerializer extends EventSerializer<Serializable> {

		ObjectOutputStream os;
		
		@Override
		public void ensureOpen(Path path) {
			if (os != null) return;
			try {
				os = new ObjectOutputStream(Files.newOutputStream(path));
			} catch (IOException e) {
				EventBus.get().handleException(e);
			}
		}

		@Override
		public boolean leaveOpen() {
			return true;
		}

		@Override
		public void close() throws IOException {
			os.close();
		}

		@Override
		public void write(Event<Serializable> u) {
			try {
				os.writeObject(u.get());
			} catch (IOException e) {
				EventBus.get().handleException(e);
			}
		}
		
	}
	
}