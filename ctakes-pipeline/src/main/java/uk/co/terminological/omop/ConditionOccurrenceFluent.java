package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import uk.co.terminological.omop.ConditionOccurrence;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface ConditionOccurrenceFluent extends ConditionOccurrence {

	// POJO setters
	// ==============

	public void setConditionOccurrenceId(Long value);

	public void setPersonId(Long value);

	public void setConditionConceptId(Integer value);

	public void setConditionStartDate(Date value);

	public void setConditionStartDatetime(Timestamp value);

	public void setConditionEndDate(Date value);

	public void setConditionEndDatetime(Timestamp value);

	public void setConditionTypeConceptId(Integer value);

	public void setConditionStatusConceptId(Integer value);

	public void setStopReason(String value);

	public void setProviderId(Long value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setConditionSourceValue(String value);

	public void setConditionSourceConceptId(Integer value);

	public void setConditionStatusSourceValue(String value);

	
	// Fluent setters
	// ==============
	
	public ConditionOccurrenceFluent withConditionOccurrenceId(Long value);
	
	public ConditionOccurrenceFluent withPersonId(Long value);
	
	public ConditionOccurrenceFluent withConditionConceptId(Integer value);
	
	public ConditionOccurrenceFluent withConditionStartDate(Date value);
	
	public ConditionOccurrenceFluent withConditionStartDatetime(Timestamp value);
	
	public ConditionOccurrenceFluent withConditionEndDate(Date value);
	
	public ConditionOccurrenceFluent withConditionEndDatetime(Timestamp value);
	
	public ConditionOccurrenceFluent withConditionTypeConceptId(Integer value);
	
	public ConditionOccurrenceFluent withConditionStatusConceptId(Integer value);
	
	public ConditionOccurrenceFluent withStopReason(String value);
	
	public ConditionOccurrenceFluent withProviderId(Long value);
	
	public ConditionOccurrenceFluent withVisitOccurrenceId(Long value);
	
	public ConditionOccurrenceFluent withVisitDetailId(Long value);
	
	public ConditionOccurrenceFluent withConditionSourceValue(String value);
	
	public ConditionOccurrenceFluent withConditionSourceConceptId(Integer value);
	
	public ConditionOccurrenceFluent withConditionStatusSourceValue(String value);
	
}
