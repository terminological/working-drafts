package uk.co.terminological.pipestream;

import java.util.Date;

public interface Job {

	public Manager getManager();
	
	public <X extends Job> Processor<X> start();
	public void completed();
	public void failed();
	
	public Date jobStart();
	public Date jobEnd();
	
	public Source getSource();
	public Sink getSink();
	public void setSink(Sink sink);
	public SinkFactory getSinkFactory();
	
	
		
	
}
