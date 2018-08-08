package uk.co.terminological.deid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
	
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

}
