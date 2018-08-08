package uk.co.terminological.deid;

public class Config {

	static String HOME = "/home/rc538";
	
	static String[] INFILE = {
			HOME+"/Data/i2b2/2014Track1/Track1-de-indentification.tar.gz",
			HOME+"/Data/i2b2/2014Track1/training-PHI-Gold-Set1.tar.gz",
			HOME+"/Data/i2b2/2014Track1/training-PHI-Gold-Set2.tar.gz"
	};
	
	static String[] TESTFILE = {
			HOME+"/Data/i2b2/2014Track1/training-PHI-Gold-Set1.tar.gz"
	};
	
	static String TRAINING_FILE = HOME+"/home/terminological/train.txt";
	static String TESTING_FILE = HOME+"/home/terminological/test.txt";
	
	static String OUTFILE = "/home/terminological/CRFmodel.ser";
	
	static String PROP = Config.class.getClassLoader().getResource("deid/CRFmodel.prop").getFile();
	
	static String GAZETTE = Config.class.getClassLoader().getResource("deid/lastNamesGazette.txt").getFile();
	
}
