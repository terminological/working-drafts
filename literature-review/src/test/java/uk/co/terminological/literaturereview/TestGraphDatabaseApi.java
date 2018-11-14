package uk.co.terminological.literaturereview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TestGraphDatabaseApi {

	private static final Logger logger = LoggerFactory.getLogger(TestGraphDatabaseApi.class);
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
	}

	@Test
	public final void testCreate() throws IOException {
		
		Path tmp = Files.createTempDirectory("neo4j");
		
		GraphDatabaseApi api = GraphDatabaseApi.create(tmp);
		logger.info("graph Db available in "+tmp);
		api.shutdown();
		
		Files.deleteIfExists(tmp);
	}

}
