package uk.co.terminological.literaturereview;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Config;

public class GraphDatabaseApi {
	
	private static GraphDatabaseApi singleton;
	private GraphDatabaseService graphDb;
	
	public static void main(String[] args) {
		File f = new File(args[0]);
		GraphDatabaseApi.create(f).waitAndShutdown();
	}
	
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
	        	waitAndShutdown();
	        }
	    } );
	}

	public void shutdown() {
		graphDb.shutdown();
	}
	
	public void waitAndShutdown() {
		System.out.println("Press Enter key to shutdown database...");
        try
        {
            System.in.read();
        }  
        catch(Exception e)
        {}  
    	graphDb.shutdown();
	}
	
	public GraphDatabaseService get() {return graphDb;}
	
	public static GraphDatabaseApi create(File graphDbPath) {
		if (singleton == null) singleton = new GraphDatabaseApi(graphDbPath);
		return singleton;
	}
	
	public static GraphDatabaseService db() {
		if (singleton == null) throw new NotInitialisedException();
		return singleton.get();
 	}
	
	
	public static class NotInitialisedException extends RuntimeException {}
}
