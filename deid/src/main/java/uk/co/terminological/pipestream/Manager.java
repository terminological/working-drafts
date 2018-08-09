package uk.co.terminological.pipestream;

import java.io.File;

public interface Manager {

	public void register(Worker worker);
	public void register(DirectoryWatcher watcher);
	
	public void pollChanges();
	public void createJob();
	
	
	
}
