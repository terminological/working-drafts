package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.UnprocessedNote;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface UnprocessedNoteFluent extends UnprocessedNote {

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

	public void setRowNumber(Integer value);

	
	// Fluent setters
	// ==============
	
	public UnprocessedNoteFluent withNoteId(Long value);
	
	public UnprocessedNoteFluent withPersonId(Long value);
	
	public UnprocessedNoteFluent withNoteEventId(Long value);
	
	public UnprocessedNoteFluent withNoteEventFieldConceptId(Integer value);
	
	public UnprocessedNoteFluent withNoteDate(Date value);
	
	public UnprocessedNoteFluent withNoteDatetime(Timestamp value);
	
	public UnprocessedNoteFluent withNoteTypeConceptId(Integer value);
	
	public UnprocessedNoteFluent withNoteClassConceptId(Integer value);
	
	public UnprocessedNoteFluent withNoteTitle(String value);
	
	public UnprocessedNoteFluent withNoteText(String value);
	
	public UnprocessedNoteFluent withEncodingConceptId(Integer value);
	
	public UnprocessedNoteFluent withLanguageConceptId(Integer value);
	
	public UnprocessedNoteFluent withProviderId(Long value);
	
	public UnprocessedNoteFluent withVisitOccurrenceId(Long value);
	
	public UnprocessedNoteFluent withVisitDetailId(Long value);
	
	public UnprocessedNoteFluent withNoteSourceValue(String value);
	
	public UnprocessedNoteFluent withRowNumber(Integer value);
	
}
