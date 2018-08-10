package uk.co.terminological.pipestream;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

public interface Source extends Iterator<InputStream> {

	public Map<String,File> getInputs();
	public Job getJob();
	
}
