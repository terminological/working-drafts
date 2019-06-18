package uk.co.terminological.ctakes;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlTransforms;
import uk.co.terminological.omop.CuiOmopMap;
import uk.co.terminological.omop.Database;
import uk.co.terminological.omop.Factory;
import uk.co.terminological.omop.NoteNlp;

public class DatabaseReader {

	static String NLP_SYSTEM = "CTAKESv1";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws SQLException {
		Database db = Database.from(Paths.get(System.getProperty("user.home"),"Dropbox/nlpCtakes/jdbc.prop"));
		HashMap<String,CuiOmopMap> cuiIndex = new HashMap<>();
		db.read().streamCuiOmopMap().forEach(
			com -> cuiIndex.put(com.getCui(),com)
		);
		
		db.query().streamInput(NLP_SYSTEM).forEach(
				n -> {
					//System.out.println(ReflectionToStringBuilder.toString(n));
					try {
						System.out.println(n.getNoteText());
						Xml html = Xml.fromHtmlStream(new ByteArrayInputStream(n.getNoteText().getBytes()));
						String clean = html.doTransform(XmlTransforms.XHTML_TO_TEXT).asString();
						System.out.println(clean);
						
						/*Note newNote = Factory.Mutable.createNote()
							.withEncodingConceptId(n.getEncodingConceptId())
							.withLanguageConceptId(n.getLanguageConceptId())
							.withNoteClassConceptId(n.getNoteClassConceptId())
							.withNoteDate(n.getNoteDate())
							.withNoteDatetime(n.getNoteDatetime())
							.withNoteEventFieldConceptId(n.getNoteEventFieldConceptId())
							.withNoteEventId(n.getNoteEventId())
							.withNoteId(n.getNoteId())
							.withNoteSourceValue(n.getNoteSourceValue())
							.withNoteText("Redacted")
							.withNoteTitle(n.getNoteTitle())
							.withNoteTypeConceptId(n.getNoteTypeConceptId())
							.withPersonId(n.getPersonId())
							.withProviderId(n.getProviderId())
							.withVisitDetailId(n.getVisitDetailId())
							.withVisitOccurrenceId(n.getVisitOccurrenceId());
							
						db.write().writeNote(newNote);*/
						
						
						
						NoteNlp nlp = Factory.Mutable.createNoteNlp();
								
						
						
					} catch (XmlException  e) {
						e.printStackTrace();
					}
				});
		
	}

}
