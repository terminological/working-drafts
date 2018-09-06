package uk.co.terminological.pipestream.idea1;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Source extends Iterator<InputStream> {

	public Map<String,File> getInputs();
	public Job getJob();
	
}
