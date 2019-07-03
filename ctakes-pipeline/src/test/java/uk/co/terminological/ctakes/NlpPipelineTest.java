package uk.co.terminological.ctakes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
	static String testSentence = "Mr. Jones is an 81 year old gentlman who lives in Okehampton. He visited me today complaining of chest pain. He has a history of hypertension and diabetes.";
	
	NlpPipeline ctakes;
	JcasOmopMapper mapper;
	Database db;
	CtakesProperties p;
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@BeforeClass
	public static void setupBeforeClass() throws URISyntaxException {
		// BasicConfigurator.configure();
		// log.info("setup before class");
		testFilePath = Paths.get(ClassLoader.getSystemResource("mtsamplesMI.txt").toURI());
		if (!Files.exists(testFilePath)) {
			throw new RuntimeException("cannot find test file");
		}
	}
	
	@Before
	public void setUp() throws Exception {
		// log.info("setup");
		CtakesProperties p = new CtakesProperties(
			Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/ctakes.prop"),
			Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/jdbc.prop")
		);
		
		log.info("Ctakes resources at: "+p.ctakesHome());

		this.p = p; 
		ctakes = new NlpPipeline(p,false);
		mapper = new NoopMapper();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRunNote() throws IOException, UIMAException {
		//fail("Not yet implemented");
		long ts = System.currentTimeMillis();
		log.info("starting parse at: "+ts);
		Input test = Factory.Mutable.createInput()
				.withEncodingConceptId(0)
				.withLanguageConceptId(0)
				.withNoteDate(Date.valueOf("2016-01-01"))
				.withNoteText(testSentence)
				.withNoteTitle("A test note");
		List<NoteNlp> ret = ctakes.runNote( test ,mapper);
		
		System.out.println(gson.toJson(ret));
	}

	//@Test
	public void testRunDocument() throws IOException, UIMAException {
		//fail("Not yet implemented");
		long ts = System.currentTimeMillis();
		Path tmp = Files.createTempFile("xmi", "xmi");
		String doc = new String(Files.readAllBytes(testFilePath));
		log.info("starting parse at: "+ts);
		String ret = ctakes.runDocument( doc,tmp);
		System.out.println(ret);
	}
	
	//@Test
	public void testRealNote() throws SQLException {
		
		db = new Database(p);
		mapper = new JcasOmopMapper(db,p.nlpSystem());
		
		db.query().fromInput(p.nlpSystem()).forEachRemaining(
			in -> {
				
				//System.out.println(in.getNoteText());
				try {
				
					try {
						NlpAudit start = Factory.Mutable.createNlpAudit()
							.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
							.withEventType(NlpPipeline.Status.PROCESSING)
							.withNlpSystem(p.nlpSystem())
							.withNlpSystemInstance(p.nlpSystemVersion())
							.withNoteId(in.getNoteId());
						db.write().writeNlpAudit(start);
					} catch (SQLException e) {
						//log - failed to grab the note - likely due to a clash for another system processing it.
						log.debug(p.nlpSystem()+" ("+p.nlpSystemVersion()+") failed to lock note: "+in.getNoteId());
						return;
					}
					
					log.info("processing note: "+in.getNoteId());
					List<NlpAudit> outcomes = new ArrayList<>();
					try {
						List<NoteNlp> ret = ctakes.runNote(in, mapper);
						System.out.println(gson.toJson(ret));
						db.write().writeBatchNoteNlp(ret);
						
						outcomes.add(
								Factory.Mutable.createNlpAudit()
								.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
								.withEventType(NlpPipeline.Status.COMPLETE)
								.withNlpSystem(p.nlpSystem())
								.withNlpSystemInstance(p.nlpSystemVersion())
								.withNoteId(in.getNoteId()));
						
					} catch (Exception e) {
						
						log.warn(p.nlpSystem()+" ("+p.nlpSystemVersion()+") failed, note: "+in.getNoteId()+", exception: "+e.getLocalizedMessage());
						
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintStream ps = new PrintStream(baos);
						e.printStackTrace(ps);
						outcomes.add(
								Factory.Mutable.createNlpAudit()
								.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
								.withEventType(NlpPipeline.Status.FAILED)
								.withNlpSystem(p.nlpSystem())
								.withNlpSystemInstance(p.nlpSystemVersion())
								.withNoteId(in.getNoteId())
								.withEventDetail(StringUtils.abbreviate(baos.toString(),512)));
						
						
						int retry = 0;
						if (in.getNlpEventType().equals(NlpPipeline.Status.RETRY))
							retry = Integer.parseInt(in.getNlpEventDetail());
						if (retry < CtakesProperties.MAX_RETRIES) {
							outcomes.add(
									Factory.Mutable.createNlpAudit()
									.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
									.withEventType(NlpPipeline.Status.RETRY)
									.withNlpSystem(p.nlpSystem())
									.withNlpSystemInstance(p.nlpSystemVersion())
									.withNoteId(in.getNoteId())
									.withEventDetail(Integer.toString(retry+1)));
						}
						
					}
					
					db.write().writeBatchNlpAudit(outcomes);
				} catch (SQLException e) {
					//Problem writing audit log
					log.error(p.nlpSystem()+" ("+p.nlpSystemVersion()+") failed to write log for note: "+in.getNoteId()+", exception: "+e.getLocalizedMessage());
					throw new RuntimeException(e);
				}
			}
		);
	}

	
	
}
