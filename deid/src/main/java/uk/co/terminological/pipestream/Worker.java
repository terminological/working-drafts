package uk.co.terminological.pipestream;

public interface Worker {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public boolean acceptJob(Job job);
	
	
}
