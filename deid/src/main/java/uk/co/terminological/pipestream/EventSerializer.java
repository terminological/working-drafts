package uk.co.terminological.pipestream;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public abstract class EventSerializer<X> implements BiConsumer<Path,Event<X>>, Closeable {
	public EventSerializer() {
		if (leaveOpen()) EventBus.get().registerCloseable(this);
	}
	public abstract void ensureOpen(Path path);
	public abstract boolean leaveOpen();
	public abstract void close() throws IOException;
	public abstract void accept(Path t, Event<X> u);
	
	
	
	public static class JavaSerialiser extends EventSerializer<Serializable> {
		
	}
	
}