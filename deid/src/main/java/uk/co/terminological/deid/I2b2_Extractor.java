package uk.co.terminological.deid;

import static uk.co.terminological.deid.Config.INFILE;
import static uk.co.terminological.deid.Config.OUTFILE;
import static uk.co.terminological.deid.Config.PROP;
import static uk.co.terminological.deid.Config.TESTFILE;
import static uk.co.terminological.deid.Config.TESTING_FILE;
import static uk.co.terminological.deid.Config.TRAINING_FILE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.fluentxml.XmlException;

/**
 * Extract sentences / tokenise and match up existing resources from  
 */
public class I2b2_Extractor {
	
	static Logger log = LoggerFactory.getLogger(I2b2_Extractor.class);
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlException {
		
		BasicConfigurator.configure();
		I2b2_Extractor extr = new I2b2_Extractor();
		
		log.info("Converting files");
		
		convert(TESTFILE, TESTING_FILE, extr);
		convert(INFILE, TRAINING_FILE, extr);
		log.info("Training model");
		trainAndWrite(OUTFILE);
		
	}
	
	private static void convert(String[] zips, String output, I2b2_Extractor extr) throws FileNotFoundException, IOException, XmlException {
		Writer out = Files.newBufferedWriter(Paths.get(output));
		
		for (String file: zips) {
			ArchiveInputStream ais = new TarArchiveInputStream(
				new GzipCompressorInputStream(
					new FileInputStream(file)
				)
			);
			extr.convert_2014(ais, out);
		}
		out.close();
	}
	
	public static void trainAndWrite(String modelOutPath) throws IOException {
		
		Properties props = StringUtils.propFileToProperties(PROP);
		props.setProperty("serializeTo", modelOutPath);
		props.setProperty("trainFile", TRAINING_FILE);
		// props.setProperty("gazette", GAZETTE);
		props.setProperty("testFile", TESTING_FILE);
		
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(modelOutPath);
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = crf.defaultReaderAndWriter();
		crf.classifyAndWriteAnswers(TESTING_FILE, readerAndWriter, true);
	}
	
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
		        	new CommonFormatConverter().toCoNLL_2003(rit.next());
		        }
		        out.flush();
		    }
		}
	}
	
	

}
