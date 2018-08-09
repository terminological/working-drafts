package uk.co.terminological.pipestream;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;

public interface DirectoryWatcher {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public void watchDirectory(File directory, FilenameFilter filter);
	public File getWatchedDirectory();
	public List<File> getChangedFiles(Date since);
	public void setProcessed(File file, Job job); 
	
	public void registerJobType(Class<Job> jobType);
	public List<Class<Job>> getJobTypes();
}
