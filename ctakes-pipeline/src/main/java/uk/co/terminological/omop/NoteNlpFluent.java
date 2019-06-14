package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.NoteNlp;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface NoteNlpFluent extends NoteNlp {

	// POJO setters
	// ==============

	public void setNoteNlpId(Long value);

	public void setNoteId(Long value);

	public void setSectionConceptId(Integer value);

	public void setSnippet(String value);

	public void setOffset(String value);

	public void setLexicalVariant(String value);

	public void setNoteNlpConceptId(Integer value);

	public void setNlpSystem(String value);

	public void setNlpDate(Date value);

	public void setNlpDatetime(Timestamp value);

	public void setTermExists(String value);

	public void setTermTemporal(String value);

	public void setTermModifiers(String value);

	public void setNoteNlpSourceConceptId(Integer value);

	public void setCustomCode(Integer value);

	
	// Fluent setters
	// ==============
	
	public NoteNlpFluent withNoteNlpId(Long value);
	
	public NoteNlpFluent withNoteId(Long value);
	
	public NoteNlpFluent withSectionConceptId(Integer value);
	
	public NoteNlpFluent withSnippet(String value);
	
	public NoteNlpFluent withOffset(String value);
	
	public NoteNlpFluent withLexicalVariant(String value);
	
	public NoteNlpFluent withNoteNlpConceptId(Integer value);
	
	public NoteNlpFluent withNlpSystem(String value);
	
	public NoteNlpFluent withNlpDate(Date value);
	
	public NoteNlpFluent withNlpDatetime(Timestamp value);
	
	public NoteNlpFluent withTermExist(String value);
	
	public NoteNlpFluent withTermTemporal(String value);
	
	public NoteNlpFluent withTermModifier(String value);
	
	public NoteNlpFluent withNoteNlpSourceConceptId(Integer value);
	
	public NoteNlpFluent withCustomCode(Integer value);
	
}
