package uk.co.terminological.pipestream;

public interface Processor<X extends Job> {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public boolean acceptJob(Job job);
	public Job getJob();
	
	
	
	
}
