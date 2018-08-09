package uk.co.terminological.pipestream;

import java.io.PrintWriter;

public interface Manager {

	public void register(Processor<? extends Job> worker);
	public void register(DirectoryWatcher watcher);
	
	public void pollChanges();
	public void createJob();
	
	public PrintWriter errorMonitor();
	public PrintWriter debugMonitor();
	
}
