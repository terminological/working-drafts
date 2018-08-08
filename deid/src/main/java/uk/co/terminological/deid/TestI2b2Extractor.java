package uk.co.terminological.deid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

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
import uk.co.terminological.fluentxml.XmlException;
import static uk.co.terminological.deid.Config.*;

public class TestI2b2Extractor {
	
	
	
	static Logger log = LoggerFactory.getLogger(TestI2b2Extractor.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlException {
		
		BasicConfigurator.configure();
		I2b2_2014_Extractor extr = new I2b2_2014_Extractor();
		
		log.info("Converting files");
		
		convert(TESTFILE, TESTING_FILE, extr);
		convert(INFILE, TRAINING_FILE, extr);
		log.info("Training model");
		trainAndWrite(OUTFILE);
		
	}
	
	private static void convert(String[] zips, String output, I2b2_2014_Extractor extr) throws FileNotFoundException, IOException, XmlException {
		Writer out = Files.newBufferedWriter(Paths.get(output));
		
		for (String file: zips) {
			ArchiveInputStream ais = new TarArchiveInputStream(
				new GzipCompressorInputStream(
					new FileInputStream(file)
				)
			);
			extr.convert(ais, out);
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

}
