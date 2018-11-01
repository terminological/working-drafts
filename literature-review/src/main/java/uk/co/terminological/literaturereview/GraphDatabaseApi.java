package uk.co.terminological.literaturereview;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Config;

public class GraphDatabaseApi {
	
	GraphDatabaseService graphDb;
	
	public GraphDatabaseApi(File graphDbPath) {
		
		//http://neo4j-contrib.github.io/neo4j-jdbc/
		Config config = Config.builder()
				.withServerDefaults()
				.build();
		
		BoltConnector bolt = config.boltConnectors().get(0);

		graphDb = new GraphDatabaseFactory()
		        .newEmbeddedDatabaseBuilder( graphDbPath )
		        .setConfig( bolt.type, "BOLT" )
		        .setConfig( bolt.enabled, "true" )
		        .setConfig( bolt.listen_address, "localhost:7687" )
		        .newGraphDatabase();
		
		Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            //TODO: hit a key to terminate the graph database
	        	graphDb.shutdown();
	        }
	    } );
	}

	
	
	
}
