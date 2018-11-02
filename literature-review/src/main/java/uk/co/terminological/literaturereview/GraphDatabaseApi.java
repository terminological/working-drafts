package uk.co.terminological.literaturereview;

import java.io.File;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphDatabaseApi {

	private static final Logger logger = LoggerFactory.getLogger(GraphDatabaseApi.class);
	
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

		logger.info("Opening graphdb in: "+graphDbPath);
		
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
		if (graphDb != null && graphDb.isAvailable(1)) {
			graphDb.shutdown();
			graphDb = null;
		}
		logger.info("graphDb is shutdown");
	}

	public void waitAndShutdown() {
		if (graphDb != null && graphDb.isAvailable(1)) {
			System.out.println("Press Enter key to shutdown neo4j...");
			try
			{
				System.in.read();
			}  
			catch(Exception e)
			{}  
			graphDb.shutdown();
			logger.info("graphDb is shutdown");
		}
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
