package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import uk.co.terminological.omop.Measurement;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface MeasurementFluent extends Measurement {

	// POJO setters
	// ==============

	public void setMeasurementId(Long value);

	public void setPersonId(Long value);

	public void setMeasurementConceptId(Integer value);

	public void setMeasurementDate(Date value);

	public void setMeasurementDatetime(Timestamp value);

	public void setMeasurementTime(String value);

	public void setMeasurementTypeConceptId(Integer value);

	public void setOperatorConceptId(Integer value);

	public void setValueAsNumber(Double value);

	public void setValueAsConceptId(Integer value);

	public void setUnitConceptId(Integer value);

	public void setRangeLow(Double value);

	public void setRangeHigh(Double value);

	public void setProviderId(Long value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setMeasurementSourceValue(String value);

	public void setMeasurementSourceConceptId(Integer value);

	public void setUnitSourceValue(String value);

	public void setValueSourceValue(String value);

	
	// Fluent setters
	// ==============
	
	public MeasurementFluent withMeasurementId(Long value);
	
	public MeasurementFluent withPersonId(Long value);
	
	public MeasurementFluent withMeasurementConceptId(Integer value);
	
	public MeasurementFluent withMeasurementDate(Date value);
	
	public MeasurementFluent withMeasurementDatetime(Timestamp value);
	
	public MeasurementFluent withMeasurementTime(String value);
	
	public MeasurementFluent withMeasurementTypeConceptId(Integer value);
	
	public MeasurementFluent withOperatorConceptId(Integer value);
	
	public MeasurementFluent withValueAsNumber(Double value);
	
	public MeasurementFluent withValueAsConceptId(Integer value);
	
	public MeasurementFluent withUnitConceptId(Integer value);
	
	public MeasurementFluent withRangeLow(Double value);
	
	public MeasurementFluent withRangeHigh(Double value);
	
	public MeasurementFluent withProviderId(Long value);
	
	public MeasurementFluent withVisitOccurrenceId(Long value);
	
	public MeasurementFluent withVisitDetailId(Long value);
	
	public MeasurementFluent withMeasurementSourceValue(String value);
	
	public MeasurementFluent withMeasurementSourceConceptId(Integer value);
	
	public MeasurementFluent withUnitSourceValue(String value);
	
	public MeasurementFluent withValueSourceValue(String value);
	
}
