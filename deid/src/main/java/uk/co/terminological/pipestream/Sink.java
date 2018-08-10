package uk.co.terminological.pipestream;

import java.io.File;
import java.util.Map;

public interface Sink {

	public Map<String,File> getOutputs();
	public Job getJob();
	
	public void consume(String output);
}
