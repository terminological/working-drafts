package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.Observation;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface ObservationFluent extends Observation {

	// POJO setters
	// ==============

	public void setObservationId(Long value);

	public void setPersonId(Long value);

	public void setObservationConceptId(Integer value);

	public void setObservationDate(Date value);

	public void setObservationDatetime(Timestamp value);

	public void setObservationTypeConceptId(Integer value);

	public void setValueAsNumber(Double value);

	public void setValueAsString(String value);

	public void setValueAsConceptId(Integer value);

	public void setQualifierConceptId(Integer value);

	public void setUnitConceptId(Integer value);

	public void setProviderId(Integer value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setObservationSourceValue(String value);

	public void setObservationSourceConceptId(Integer value);

	public void setUnitSourceValue(String value);

	public void setQualifierSourceValue(String value);

	public void setObservationEventId(Long value);

	public void setObsEventFieldConceptId(Integer value);

	public void setValueAsDatetime(Timestamp value);

	
	// Fluent setters
	// ==============
	
	public ObservationFluent withObservationId(Long value);
	
	public ObservationFluent withPersonId(Long value);
	
	public ObservationFluent withObservationConceptId(Integer value);
	
	public ObservationFluent withObservationDate(Date value);
	
	public ObservationFluent withObservationDatetime(Timestamp value);
	
	public ObservationFluent withObservationTypeConceptId(Integer value);
	
	public ObservationFluent withValueAsNumber(Double value);
	
	public ObservationFluent withValueAsString(String value);
	
	public ObservationFluent withValueAsConceptId(Integer value);
	
	public ObservationFluent withQualifierConceptId(Integer value);
	
	public ObservationFluent withUnitConceptId(Integer value);
	
	public ObservationFluent withProviderId(Integer value);
	
	public ObservationFluent withVisitOccurrenceId(Long value);
	
	public ObservationFluent withVisitDetailId(Long value);
	
	public ObservationFluent withObservationSourceValue(String value);
	
	public ObservationFluent withObservationSourceConceptId(Integer value);
	
	public ObservationFluent withUnitSourceValue(String value);
	
	public ObservationFluent withQualifierSourceValue(String value);
	
	public ObservationFluent withObservationEventId(Long value);
	
	public ObservationFluent withObsEventFieldConceptId(Integer value);
	
	public ObservationFluent withValueAsDatetime(Timestamp value);
	
}
