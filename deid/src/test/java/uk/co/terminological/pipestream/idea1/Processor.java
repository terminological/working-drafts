package uk.co.terminological.pipestream.idea1;

public interface Processor<X extends Job> {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public X getJob();
	
	public void initialise();
	public void process();
	public void finalise();
	
	
}
