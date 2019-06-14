package uk.co.terminological.ctakes;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import uk.co.terminological.omop.Input;
import uk.co.terminological.omop.NlpAudit;
import uk.co.terminological.omop.NoteNlp;

public class NlpPipelineTest {

	static Logger log = LoggerFactory.getLogger(NlpPipelineTest.class);
	static Path testFilePath;
	
	NlpPipeline ctakes;
	JcasOmopMapper mapper;
	Database db; 
	
	static String NLP_SYSTEM = "CTAKESv1";
	static String NLP_SYSTEM_VERSION = "Pipeline tester";
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
		mapper = new JcasOmopMapper(db,NLP_SYSTEM);
		ctakes = new NlpPipeline(p.getProperty("umls.user"),p.getProperty("umls.pw"),ctakesHome.toString());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRunNote() throws IOException, UIMAException {
		//fail("Not yet implemented");
		long ts = System.currentTimeMillis();
		String doc = new String(Files.readAllBytes(testFilePath));
		log.info("starting parse at: "+ts);
		Input test = Factory.Mutable.createInput()
				.withEncodingConceptId(0)
				.withLanguageConceptId(0)
				.withNoteDate(Date.valueOf("2016-01-01"))
				.withNoteText(doc)
				.withNoteTitle("A test note");
		List<NoteNlp> ret = ctakes.runNote( test ,mapper);
		
		System.out.println(gson.toJson(ret));
	}

	@Test
	public void testRunDocument() throws IOException, UIMAException {
		//fail("Not yet implemented");
		long ts = System.currentTimeMillis();
		Path tmp = Files.createTempFile("xmi", "xmi");
		String doc = new String(Files.readAllBytes(testFilePath));
		log.info("starting parse at: "+ts);
		String ret = ctakes.runDocument( doc,tmp);
		System.out.println(ret);
	}
	
	@Test
	public void testRealNote() throws SQLException {
		db.query().fromInput(NLP_SYSTEM).forEachRemaining(
			in -> {
				//System.out.println(in.getNoteText());
				try {
				
					NlpAudit start = Factory.Mutable.createNlpAudit()
						.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
						.withEventType(NlpPipeline.Status.PROCESSING)
						.withNlpSystem(NLP_SYSTEM)
						.withNlpSystemInstance(NLP_SYSTEM_VERSION)
						.withNoteId(in.getNoteId());
					db.write().writeNlpAudit(start);
					
					NlpAudit outcome;
					try {
						List<NoteNlp> ret = ctakes.runNote(in, mapper);
						System.out.println(gson.toJson(ret));
						db.write().writeBatchNoteNlp(ret);
						
						outcome = Factory.Mutable.createNlpAudit()
								.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
								.withEventType(NlpPipeline.Status.COMPLETE)
								.withNlpSystem(NLP_SYSTEM)
								.withNlpSystemInstance(NLP_SYSTEM_VERSION)
								.withNoteId(in.getNoteId());
						
					} catch (Exception e) {
						
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						e.printStackTrace(ps);
						outcome = Factory.Mutable.createNlpAudit()
								.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
								.withEventType(NlpPipeline.Status.FAILED)
								.withNlpSystem(NLP_SYSTEM)
								.withNlpSystemInstance(NLP_SYSTEM_VERSION)
								.withNoteId(in.getNoteId())
								.withEventDetail(baos.toString());
						
					}
					
					db.write().writeNlpAudit(outcome);
				} catch (SQLException e) {
					//Problem writing audit log
					throw new RuntimeException(e);
				}
			}
		);
	}

	
	
}
