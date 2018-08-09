package uk.co.terminological.deid;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.fluentxml.XmlException;

/**
 * Extract sentences / tokenise and match up existing resources from  
 */
public class I2b2_2014_Extractor {
	
	static Logger log = LoggerFactory.getLogger(I2b2_2014_Extractor.class);
	
	public void convert_2014(ArchiveInputStream zipIn, Writer out) throws XmlException, IOException {
		//public void readZipStream(InputStream in) throws IOException {
		ArchiveEntry entry;
		while ((entry = zipIn.getNextEntry()) != null) {
		    if (entry.getName().endsWith("xml")) {
		    	log.debug("processing: "+entry.getName());

		    	// this stops Xml from closing the underlynig ZipInputStream
		    	InputStream tmp = new FilterInputStream(zipIn) {
		            @Override
		            public void close() throws IOException {
		                // do nothing!
		            }
		        };
		    	
		        I2b2_2014_Format infile = new I2b2_2014_Format(tmp,entry.getName());
		        Iterator<Record> rit = infile.getRecords();
		        while (rit.hasNext()) {
		        	CommonFormatConverter.get().writeToCoNLL_2003(rit.next(),out);
		        }
		        out.flush();
		    }
		}
	}
	
	/**
	 * flushes writer but does not close it
	 * @param in
	 * @param out
	 * @throws XmlException - if it can't parse the XML
	 * @throws IOException - if it can't write to the output
	 */
	public void convert(InputStream in, String id, Writer out) throws XmlException, IOException {
		
		I2b2_2014_Format infile = new I2b2_2014_Format(in,id); 
		Record r = infile.getRecords().next();
		
		
		CommonFormatConverter.get().writeToCoNLL_2003(r,out);
		
		out.flush();
		
		
	}

}
