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
import java.sql.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.Configurator;
import org.apache.uima.UIMAException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.apache.ctakes.assertion.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.co.terminological.omop.Database;
import uk.co.terminological.omop.Factory;
import uk.co.terminological.omop.NoteNlp;
import uk.co.terminological.omop.UnprocessedNote;

public class NlpPipelineTest {

	static Logger log = LoggerFactory.getLogger(NlpPipelineTest.class);
	static Path testFilePath;
	
	NlpPipeline ctakes;
	JcasOmopMapper mapper;
	Database db; 

	@BeforeClass
	public static void setupBeforeClass() throws URISyntaxException {
		BasicConfigurator.configure();
		log.info("setup before class");
		testFilePath = Paths.get(ClassLoader.getSystemResource("mtsamplesMI.txt").toURI());
		if (!Files.exists(testFilePath)) {
			throw new RuntimeException("cannot find test file");
		}
	}
	
	@Before
	public void setUp() throws Exception {
		log.info("setup");
		Properties p = new Properties();
		p.load(Files.newInputStream(
				Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/ctakes.prop")));
		Path ctakesHome = Paths.get(System.getProperty("user.home"),p.getProperty("ctakes.resources"));
		log.info("Ctakes resources at: "+ctakesHome);
		//environmentVariables.set(AlternateLvgAnnotator.CTAKES_HOME, ctakesHome.toString());
		db = new Database(Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/jdbc.prop"));
		mapper = new JcasOmopMapper(db);
		ctakes = new NlpPipeline(p.getProperty("umls.user"),p.getProperty("umls.pw"),ctakesHome.toString());
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
		UnprocessedNote test = Factory.Mutable.createUnprocessedNote()
				.withEncodingConceptId(0)
				.withLanguageConceptId(0)
				.withNoteDate(Date.valueOf("2016-01-01"))
				.withNoteText(doc)
				.withNoteTitle("A test note");
		List<NoteNlp> ret = ctakes.runNote( test ,mapper);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		System.out.println(gson.toJson(ret));
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
