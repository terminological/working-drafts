package uk.co.terminological.literaturereview;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class TestGraphDatabaseApi {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testCreate() {
		
		File tmp = Files.createTempDir();
		GraphDatabaseApi api = GraphDatabaseApi.create(tmp);
		api.shutdown();
		tmp.deleteOnExit();
	}

}
