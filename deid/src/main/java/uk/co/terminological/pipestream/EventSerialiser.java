package uk.co.terminological.pipestream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public abstract class EventSerialiser<X> implements BiConsumer<Path,Event<X>>, Closeable {
	public EventSerialiser() {
		if (leaveOpen()) EventBus.get().registerCloseable(this);
	}
	public abstract void ensureOpen(Path path);
	public abstract boolean leaveOpen();
	public abstract void close() throws IOException;
	public abstract void accept(Path t, Event<X> u);
}