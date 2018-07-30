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

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;
import uk.co.terminological.fluentxml.XmlException;

public class TestI2b2Extractor {
	
	static final String[] INFILE = {
			"/media/data/Data/i2b2/2014Track1/Track1-de-indentification.tar.gz",
			"/media/data/Data/i2b2/2014Track1/training-PHI-Gold-Set1.tar.gz",
			"/media/data/Data/i2b2/2014Track1/training-PHI-Gold-Set2.tar.gz"
	};
	
	static final String TRAINING_FILE = "/home/terminological/output.txt";
	
	static final String OUTFILE = "/home/terminological/CRFmodel.ser";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlException {
		BasicConfigurator.configure();
		I2b2Extractor extr = new I2b2Extractor();
		
		Writer out = Files.newBufferedWriter(Paths.get(TRAINING_FILE));
		
		for (String file: INFILE) {
		ArchiveInputStream ais = new TarArchiveInputStream(
			new GzipCompressorInputStream(
				new FileInputStream(file)
			)
		);
		extr.convert(ais, out);
		}
		
		out.close();
		
		trainAndWrite(OUTFILE);
		
	}
	
	public static void trainAndWrite(String modelOutPath) {
		String prop = TestI2b2Extractor.class.getClassLoader().getResource("deid/i2b2example.xml").getFile();
		Properties props = StringUtils.propFileToProperties(prop);
		props.setProperty("serializeTo", modelOutPath);
		props.setProperty("trainFile", TRAINING_FILE);
		SeqClassifierFlags flags = new SeqClassifierFlags(props);
		CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
		crf.train();
		crf.serializeClassifier(modelOutPath);
	}

}
