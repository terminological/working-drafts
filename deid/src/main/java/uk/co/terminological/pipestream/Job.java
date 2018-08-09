package uk.co.terminological.pipestream;

import java.util.List;

public interface Job {

	public Source getSource();
	public Sink getSink();
	public void setSink(Sink sink);
	public SinkFactory<Job> getSinkFactory();
	public String getMetadata(String key);
	public void markCompleted();
		
	
}
