package uk.co.terminological.pipestream;

import java.io.PrintWriter;

public interface Job {

	public Manager getManager();
	
	public void completed();
	public void failed();
	
	
	
	public Source getSource();
	public Sink getSink();
	public void setSink(Sink sink);
	public SinkFactory getSinkFactory();
	
	
		
	
}
