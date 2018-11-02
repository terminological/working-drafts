package uk.co.terminological.literaturereview;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class TestGraphDatabaseApi {

	private static final Logger logger = LoggerFactory.getLogger(TestGraphDatabaseApi.class);
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
	}

	@Test
	public final void testCreate() {
		
		File tmp = Files.createTempDir();
		
		GraphDatabaseApi api = GraphDatabaseApi.create(tmp);
		logger.info("graph Db available in "+tmp);
		api.shutdown();
		
		tmp.deleteOnExit();
	}

}
