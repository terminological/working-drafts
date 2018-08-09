package uk.co.terminological.pipestream;

public interface SinkFactory<X extends Job> {

	public Sink buildSink(X job);
	
}
