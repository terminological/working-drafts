package uk.co.terminological.pipestream;

import java.io.File;

public interface Sink<X extends Job> {

	public File getOutput();
	public X getJob();
	
	public void consume(String output);
}
