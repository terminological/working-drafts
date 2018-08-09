package uk.co.terminological.pipestream;

public interface Job {

	public Source getSource();
	public Sink getSink();
	public String getMetadata(String key);
	
}
