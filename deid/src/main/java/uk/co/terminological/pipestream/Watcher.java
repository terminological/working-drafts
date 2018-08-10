package uk.co.terminological.pipestream;

import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;

public interface Watcher {

	public default void registerWith(Manager manager) {
		manager.register(this);
	};
	
	public List<File> getFiles();
	public List<File> getChangedFiles(Date since);
		
	public void setProcessed(File file, Job job); 
	
	public void registerJobType(Class<Job> jobType);
	public List<Class<Job>> getJobTypes();
	
	
	public static interface Directory extends Watcher {
		
		public void watchDirectory(File directory, FilenameFilter filter);
		public File getDirectory();
		public void setDirectory(File directory);
		
	}
	
	public static interface File extends Watcher {
		
		public void watchFile(File file);
		public File getFile();
		public void setFile(File file);
		
	}
	
	public static interface Sink extends Watcher {
		
		public void watchSink(Sink sink);
		public Sink getSink();
		public void setSink(Sink sink);
		
	}
	
}
