package uk.co.terminological.pipestream;

import java.util.List;

public interface Job {

	public Source getSource();
	public SinkFactory getSinkFactory();
	public String getMetadata(String key);
	public void markCompleted();
		
	
}
