package uk.co.terminological.pipestream;

public interface Processor {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public boolean acceptJob(Job job);
	
	
}
