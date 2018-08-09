package uk.co.terminological.pipestream;

import java.io.File;

public interface Sink {

	public File getOutput();
	public Job getJob();
	
	public void consume(String output);
}
