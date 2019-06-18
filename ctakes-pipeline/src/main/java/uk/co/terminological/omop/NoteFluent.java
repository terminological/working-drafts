package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import uk.co.terminological.omop.Note;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface NoteFluent extends Note {

	// POJO setters
	// ==============

	public void setNoteId(Long value);

	public void setPersonId(Long value);

	public void setNoteEventId(Long value);

	public void setNoteEventFieldConceptId(Integer value);

	public void setNoteDate(Date value);

	public void setNoteDatetime(Timestamp value);

	public void setNoteTypeConceptId(Integer value);

	public void setNoteClassConceptId(Integer value);

	public void setNoteTitle(String value);

	public void setNoteText(String value);

	public void setEncodingConceptId(Integer value);

	public void setLanguageConceptId(Integer value);

	public void setProviderId(Long value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setNoteSourceValue(String value);

	
	// Fluent setters
	// ==============
	
	public NoteFluent withNoteId(Long value);
	
	public NoteFluent withPersonId(Long value);
	
	public NoteFluent withNoteEventId(Long value);
	
	public NoteFluent withNoteEventFieldConceptId(Integer value);
	
	public NoteFluent withNoteDate(Date value);
	
	public NoteFluent withNoteDatetime(Timestamp value);
	
	public NoteFluent withNoteTypeConceptId(Integer value);
	
	public NoteFluent withNoteClassConceptId(Integer value);
	
	public NoteFluent withNoteTitle(String value);
	
	public NoteFluent withNoteText(String value);
	
	public NoteFluent withEncodingConceptId(Integer value);
	
	public NoteFluent withLanguageConceptId(Integer value);
	
	public NoteFluent withProviderId(Long value);
	
	public NoteFluent withVisitOccurrenceId(Long value);
	
	public NoteFluent withVisitDetailId(Long value);
	
	public NoteFluent withNoteSourceValue(String value);
	
}
