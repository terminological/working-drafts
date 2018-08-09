package uk.co.terminological.pipestream;

import java.io.File;

public interface Manager {

	public void register(Processor worker);
	public void register(DirectoryWatcher watcher);
	
	public void pollChanges();
	public void createJob();
	
	
	
}
