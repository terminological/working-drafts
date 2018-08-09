package uk.co.terminological.pipestream;

public interface SinkFactory {

	public Sink buildSink(Job job);
	
}
