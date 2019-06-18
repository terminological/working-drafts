package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.DrugExposure;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface DrugExposureFluent extends DrugExposure {

	// POJO setters
	// ==============

	public void setDrugExposureId(Long value);

	public void setPersonId(Long value);

	public void setDrugConceptId(Integer value);

	public void setDrugExposureStartDate(Date value);

	public void setDrugExposureStartDatetime(Timestamp value);

	public void setDrugExposureEndDate(Date value);

	public void setDrugExposureEndDatetime(Timestamp value);

	public void setVerbatimEndDate(Date value);

	public void setDrugTypeConceptId(Integer value);

	public void setStopReason(String value);

	public void setRefills(Integer value);

	public void setQuantity(Double value);

	public void setDaysSupply(Integer value);

	public void setSig(String value);

	public void setRouteConceptId(Integer value);

	public void setLotNumber(String value);

	public void setProviderId(Long value);

	public void setVisitOccurrenceId(Long value);

	public void setVisitDetailId(Long value);

	public void setDrugSourceValue(String value);

	public void setDrugSourceConceptId(Integer value);

	public void setRouteSourceValue(String value);

	public void setDoseUnitSourceValue(String value);

	
	// Fluent setters
	// ==============
	
	public DrugExposureFluent withDrugExposureId(Long value);
	
	public DrugExposureFluent withPersonId(Long value);
	
	public DrugExposureFluent withDrugConceptId(Integer value);
	
	public DrugExposureFluent withDrugExposureStartDate(Date value);
	
	public DrugExposureFluent withDrugExposureStartDatetime(Timestamp value);
	
	public DrugExposureFluent withDrugExposureEndDate(Date value);
	
	public DrugExposureFluent withDrugExposureEndDatetime(Timestamp value);
	
	public DrugExposureFluent withVerbatimEndDate(Date value);
	
	public DrugExposureFluent withDrugTypeConceptId(Integer value);
	
	public DrugExposureFluent withStopReason(String value);
	
	public DrugExposureFluent withRefill(Integer value);
	
	public DrugExposureFluent withQuantity(Double value);
	
	public DrugExposureFluent withDaysSupply(Integer value);
	
	public DrugExposureFluent withSig(String value);
	
	public DrugExposureFluent withRouteConceptId(Integer value);
	
	public DrugExposureFluent withLotNumber(String value);
	
	public DrugExposureFluent withProviderId(Long value);
	
	public DrugExposureFluent withVisitOccurrenceId(Long value);
	
	public DrugExposureFluent withVisitDetailId(Long value);
	
	public DrugExposureFluent withDrugSourceValue(String value);
	
	public DrugExposureFluent withDrugSourceConceptId(Integer value);
	
	public DrugExposureFluent withRouteSourceValue(String value);
	
	public DrugExposureFluent withDoseUnitSourceValue(String value);
	
}
