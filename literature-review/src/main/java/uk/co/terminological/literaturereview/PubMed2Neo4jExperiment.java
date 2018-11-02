package uk.co.terminological.literaturereview;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pubmedclient.BibliographicApis;

public class PubMed2Neo4jExperiment {
	
	//
	
	// Event types
	public static final String 
	
	// Event names
	
	// Handlers
	
	// Metadata keys
	
	public static void main(String args[]) throws IOException {

		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		BibliographicApis biblioApi = BibliographicApis.create(filePath)
		execute(graphApi, biblioApi);
	}
	
	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi) throws IOException {
		
		EventBus.get()
			.withApi(graphApi)
			.withApi(biblioApi)
		
	}

}
