package uk.co.terminological.nlptools;

import java.io.IOException;

public class SerialisationHelper {

	//see https://bugs.openjdk.java.net/browse/JDK-8201131
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException { 
		in.defaultReadObject(); 
	}
}
