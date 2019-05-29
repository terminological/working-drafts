package uk.co.terminological.ctakes;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.uima.UIMAException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NlpPipelineTest {

	static Logger log = LoggerFactory.getLogger(NlpPipelineTest.class);
	static Path testFilePath;
	
	NlpPipeline ctakes;
	
	@Rule
	  public final EnvironmentVariables environmentVariables
	    = new EnvironmentVariables();

	@BeforeClass
	public static void setupBeforeClass() throws URISyntaxException {
		testFilePath = Paths.get(ClassLoader.getSystemResource("mtsamplesMI.txt").toURI());
		if (!Files.exists(testFilePath)) {
			throw new RuntimeException("cannot find test file");
		}
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception {
		Properties p = new Properties();
		p.load(Files.newInputStream(
				Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/ctakes.prop")));
		ctakes = new NlpPipeline(p.getProperty("umls.user"),p.getProperty("umls.pw"));
		Path ctakesHome = Paths.get(System.getProperty("user.home"),p.getProperty("ctakes.resources"));
		log.info("Ctakes resources at: "+ctakesHome);
		environmentVariables.set("CTAKES_HOME", ctakesHome.toString());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() throws IOException, UIMAException {
		//fail("Not yet implemented");
		long ts = System.currentTimeMillis();
		String doc = new String(Files.readAllBytes(testFilePath));
		log.info("starting parse at: "+ts);
		String ret = ctakes.runDocument( doc );
		System.out.println(ret);
	}

	/*@Test
	public void testRunDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void test( String[] argv ) throws IllegalAccessException, InvocationTargetException, Exception {
		long ts = 0;
		long ts1 = 0;
		int count = 0;
		
		ts = System.currentTimeMillis();
		 
		ts1 += System.currentTimeMillis() - ts;
		System.out.println( "ctakes, " + count + ", " + ts1 / 1000 + " seconds.");
		
		
		
		File indir = new File( "input/" );
		for( File file : indir.listFiles() ) {
			if( file.getName().startsWith( "." ) ) {
				continue;
			}
			if( !file.getName().endsWith( ".txt" ) ) {
				continue;
			}
			System.out.println( file.getName() );

			String doc = IOUtils.toString( new FileReader( file ) );
			FileWriter outfile = new FileWriter( new File( "output/" + file.getName() ) );

			ts = System.currentTimeMillis();
			String ret = ctakes.runDocument( doc );
			ts1 += System.currentTimeMillis() - ts;
			count += 1;
			System.out.println( "ctakes, " + count + ", " + ts1 / 1000 + " seconds.");
			outfile.write( ret );
			outfile.close();
		
		}
		return;
	}*/
	
}
