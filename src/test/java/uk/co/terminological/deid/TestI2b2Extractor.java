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

public class TestI2b2Extractor {
	
	static final String[] INFILE = {
			"/media/data/Data/i2b2/2014Track1/Track1-de-indentification.tar.gz",
			"/media/data/Data/i2b2/2014Track1/training-PHI-Gold-Set1.tar.gz",
			"/media/data/Data/i2b2/2014Track1/training-PHI-Gold-Set2.tar.gz"
	};
	
	static final String[] TESTFILE = {
			"/media/data/Data/i2b2/2014Track1/training-PHI-Gold-Set1.tar.gz"
	};
	
	static final String TRAINING_FILE = "/home/terminological/train.txt";
	static final String TESTING_FILE = "/home/terminological/test.txt";
	
	static final String OUTFILE = "/home/terminological/CRFmodel.ser";
	
	static final String PROP = TestI2b2Extractor.class.getClassLoader().getResource("deid/CRFmodel.prop").getFile();
	static final String GAZETTE = TestI2b2Extractor.class.getClassLoader().getResource("deid/lastNamesGazette.txt").getFile();
	
	static Logger log = LoggerFactory.getLogger(TestI2b2Extractor.class);
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlException {
		
		BasicConfigurator.configure();
		I2b2Extractor extr = new I2b2Extractor();
		
		log.info("Converting files");
		
		convert(TESTFILE, TESTING_FILE, extr);
		convert(INFILE, TRAINING_FILE, extr);
		log.info("Training model");
		trainAndWrite(OUTFILE);
		
	}
	
	private static void convert(String[] zips, String output, I2b2Extractor extr) throws FileNotFoundException, IOException, XmlException {
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
	
	public static void trainAndWrite(String modelOutPath) {
		
		Properties props = StringUtils.propFileToProperties(PROP);
		props.setProperty("serializeTo", modelOutPath);
		props.setProperty("trainFile", TRAINING_FILE);
		props.setProperty("gazette", GAZETTE);
		props.setProperty("testFile", TESTING_FILE);
		
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(modelOutPath);
		DocumentReaderAndWriter<CoreLabel> readerAndWriter = crf.defaultReaderAndWriter();
		crf.printFirstOrderProbs(TESTING_FILE, readerAndWriter);
	}

}
