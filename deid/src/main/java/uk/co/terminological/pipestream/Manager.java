package uk.co.terminological.pipestream;

import java.io.PrintWriter;
import java.util.List;

public interface Manager {

	public void register(Processor<? extends Job> worker);
	public void register(DirectoryWatcher watcher);
	
	public void pollChanges();
	public List<Job> jobHistory();
	public void createJob();
	
	public PrintWriter errorMonitor();
	public PrintWriter debugMonitor();
	
}
