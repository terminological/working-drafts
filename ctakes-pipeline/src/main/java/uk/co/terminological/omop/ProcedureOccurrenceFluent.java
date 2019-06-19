package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.ProcedureOccurrence;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface ProcedureOccurrenceFluent extends ProcedureOccurrence {

	// POJO setters
	// ==============

	public void setProcedureOccurrenceId(Long value);

	public void setPersonId(Long value);

	public void setProcedureConceptId(Integer value);

	public void setProcedureDate(Date value);

	public void setProcedureDatetime(Timestamp value);

	public void setProcedureTypeConceptId(Integer value);

	public void setModifierConceptId(Integer value);

	public void setQuantity(Integer value);

	public void setProviderId(Long value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setProcedureSourceValue(String value);

	public void setProcedureSourceConceptId(Integer value);

	public void setModifierSourceValue(String value);

	
	// Fluent setters
	// ==============
	
	public ProcedureOccurrenceFluent withProcedureOccurrenceId(Long value);
	
	public ProcedureOccurrenceFluent withPersonId(Long value);
	
	public ProcedureOccurrenceFluent withProcedureConceptId(Integer value);
	
	public ProcedureOccurrenceFluent withProcedureDate(Date value);
	
	public ProcedureOccurrenceFluent withProcedureDatetime(Timestamp value);
	
	public ProcedureOccurrenceFluent withProcedureTypeConceptId(Integer value);
	
	public ProcedureOccurrenceFluent withModifierConceptId(Integer value);
	
	public ProcedureOccurrenceFluent withQuantity(Integer value);
	
	public ProcedureOccurrenceFluent withProviderId(Long value);
	
	public ProcedureOccurrenceFluent withVisitOccurrenceId(Long value);
	
	public ProcedureOccurrenceFluent withVisitDetailId(Long value);
	
	public ProcedureOccurrenceFluent withProcedureSourceValue(String value);
	
	public ProcedureOccurrenceFluent withProcedureSourceConceptId(Integer value);
	
	public ProcedureOccurrenceFluent withModifierSourceValue(String value);
	
}
