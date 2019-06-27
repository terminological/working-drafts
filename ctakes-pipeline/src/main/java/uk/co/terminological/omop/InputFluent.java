package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.Input;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface InputFluent extends Input {

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

	public void setNlpEventType(String value);

	public void setNlpSystem(String value);

	public void setNlpSystemInstance(String value);

	public void setNlpEventTime(Timestamp value);

	public void setNlpEventDetail(String value);

	public void setNlpPriority(Integer value);

	public void setRowNumber(Integer value);

	
	// Fluent setters
	// ==============
	
	public InputFluent withNoteId(Long value);
	
	public InputFluent withPersonId(Long value);
	
	public InputFluent withNoteEventId(Long value);
	
	public InputFluent withNoteEventFieldConceptId(Integer value);
	
	public InputFluent withNoteDate(Date value);
	
	public InputFluent withNoteDatetime(Timestamp value);
	
	public InputFluent withNoteTypeConceptId(Integer value);
	
	public InputFluent withNoteClassConceptId(Integer value);
	
	public InputFluent withNoteTitle(String value);
	
	public InputFluent withNoteText(String value);
	
	public InputFluent withEncodingConceptId(Integer value);
	
	public InputFluent withLanguageConceptId(Integer value);
	
	public InputFluent withProviderId(Long value);
	
	public InputFluent withVisitOccurrenceId(Long value);
	
	public InputFluent withVisitDetailId(Long value);
	
	public InputFluent withNoteSourceValue(String value);
	
	public InputFluent withNlpEventType(String value);
	
	public InputFluent withNlpSystem(String value);
	
	public InputFluent withNlpSystemInstance(String value);
	
	public InputFluent withNlpEventTime(Timestamp value);
	
	public InputFluent withNlpEventDetail(String value);
	
	public InputFluent withNlpPriority(Integer value);
	
	public InputFluent withRowNumber(Integer value);
	
}
