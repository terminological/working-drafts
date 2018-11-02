package uk.co.terminological.literaturereview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pubmedclient.BibliographicApis;

public class PubMed2Neo4jExperiment {
	
	// default settings
	// TODO: Change this
	static Properties prop = System.getProperties();
	static Path DEFAULT_GRAPH_OPTIONS = Paths.get("/media/data/Data/neo4j.conf");
	static Path BIBLIOGRAPHY_OPTIONS = Paths.get(prop.getProperty("user.home"),"Dropbox/secrets.prop");
	
	// Event types
	public static final String 
	
	// Event names
	
	// Handlers
	
	// Metadata keys
	
	public static void main(String args[]) throws IOException {

		Path filePath = BIBLIOGRAPHY_OPTIONS;
		Path graphOptionsPath = DEFAULT_GRAPH_OPTIONS;
		
		if (args.length == 2) {
			filePath = Paths.get(args[0]);
			graphOptionsPath = Paths.get(args[1]);
		}
		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		BibliographicApis biblioApi = BibliographicApis.create(filePath);
		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphOptionsPath);
		execute(graphApi, biblioApi);
	}
	
	public static void execute(GraphDatabaseApi graphApi, BibliographicApis biblioApi) throws IOException {
		
		EventBus.get()
			.withApi(graphApi)
			.withApi(biblioApi)
		
	}

}
