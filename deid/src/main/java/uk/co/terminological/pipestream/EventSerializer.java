package uk.co.terminological.pipestream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class EventSerializer<X> {
	public abstract void write(X u, Path path);

	
	public static final EventSerializer<Serializable> JAVA_SERIALIZER = new EventSerializer<Serializable>() {
		@Override
		public void write(Serializable u, Path path) {
			try {
				ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(path));
				os.writeObject(u);
				os.close();
			} catch (IOException e) {
				EventBus.get().handleException(e);
			}
		}
	};

	public static final EventSerializer<Object> TO_STRING_FILE_WRITER = new EventSerializer<Object>() {
			@Override
			public void write(Object u, Path path) {
				try {
					OutputStream os = Files.newOutputStream(path);
					os.write(u.toString().getBytes(Charset.defaultCharset()));
					os.close();
				} catch (IOException e) {
					EventBus.get().handleException(e);
			}
		}
	};

}