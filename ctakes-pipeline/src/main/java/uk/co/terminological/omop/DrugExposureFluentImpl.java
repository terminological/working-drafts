package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.DrugExposureFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class DrugExposureFluentImpl extends Observable implements DrugExposure, DrugExposureFluent  {

	public static DrugExposureFluent create() {
		return new DrugExposureFluentImpl();
	}

	// Fields
	// ======

	private Long _drugExposureId;
	private Long _personId;
	private Integer _drugConceptId;
	private Date _drugExposureStartDate;
	private Timestamp _drugExposureStartDatetime;
	private Date _drugExposureEndDate;
	private Timestamp _drugExposureEndDatetime;
	private Date _verbatimEndDate;
	private Integer _drugTypeConceptId;
	private String _stopReason;
	private Integer _refills;
	private Double _quantity;
	private Integer _daysSupply;
	private String _sig;
	private Integer _routeConceptId;
	private String _lotNumber;
	private Long _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _drugSourceValue;
	private Integer _drugSourceConceptId;
	private String _routeSourceValue;
	private String _doseUnitSourceValue;

	// Public constructor
	// ==================

	public DrugExposureFluentImpl() {}

	public DrugExposureFluentImpl(
		Long _drugExposureId,
		Long _personId,
		Integer _drugConceptId,
		Date _drugExposureStartDate,
		Timestamp _drugExposureStartDatetime,
		Date _drugExposureEndDate,
		Timestamp _drugExposureEndDatetime,
		Date _verbatimEndDate,
		Integer _drugTypeConceptId,
		String _stopReason,
		Integer _refills,
		Double _quantity,
		Integer _daysSupply,
		String _sig,
		Integer _routeConceptId,
		String _lotNumber,
		Long _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _drugSourceValue,
		Integer _drugSourceConceptId,
		String _routeSourceValue,
		String _doseUnitSourceValue
	) {
		this._drugExposureId = _drugExposureId;
		this._personId = _personId;
		this._drugConceptId = _drugConceptId;
		this._drugExposureStartDate = _drugExposureStartDate;
		this._drugExposureStartDatetime = _drugExposureStartDatetime;
		this._drugExposureEndDate = _drugExposureEndDate;
		this._drugExposureEndDatetime = _drugExposureEndDatetime;
		this._verbatimEndDate = _verbatimEndDate;
		this._drugTypeConceptId = _drugTypeConceptId;
		this._stopReason = _stopReason;
		this._refills = _refills;
		this._quantity = _quantity;
		this._daysSupply = _daysSupply;
		this._sig = _sig;
		this._routeConceptId = _routeConceptId;
		this._lotNumber = _lotNumber;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._drugSourceValue = _drugSourceValue;
		this._drugSourceConceptId = _drugSourceConceptId;
		this._routeSourceValue = _routeSourceValue;
		this._doseUnitSourceValue = _doseUnitSourceValue;
	}
	
	@SuppressWarnings("unchecked")
	public DrugExposureFluentImpl(DrugExposure clone) {
		this._drugExposureId = clone.getDrugExposureId();
		this._personId = clone.getPersonId();
		this._drugConceptId = clone.getDrugConceptId();
		this._drugExposureStartDate = (Date) clone.getDrugExposureStartDate().clone();
		this._drugExposureStartDatetime = (Timestamp) clone.getDrugExposureStartDatetime().clone();
		this._drugExposureEndDate = (Date) clone.getDrugExposureEndDate().clone();
		this._drugExposureEndDatetime = (Timestamp) clone.getDrugExposureEndDatetime().clone();
		this._verbatimEndDate = (Date) clone.getVerbatimEndDate().clone();
		this._drugTypeConceptId = clone.getDrugTypeConceptId();
		this._stopReason = clone.getStopReason();
		this._refills = clone.getRefills();
		this._quantity = clone.getQuantity();
		this._daysSupply = clone.getDaysSupply();
		this._sig = clone.getSig();
		this._routeConceptId = clone.getRouteConceptId();
		this._lotNumber = clone.getLotNumber();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._drugSourceValue = clone.getDrugSourceValue();
		this._drugSourceConceptId = clone.getDrugSourceConceptId();
		this._routeSourceValue = clone.getRouteSourceValue();
		this._doseUnitSourceValue = clone.getDoseUnitSourceValue();
	}
	
	public DrugExposureFluentImpl clone() {
		return new DrugExposureFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getDrugExposureId() {
		return _drugExposureId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Integer getDrugConceptId() {
		return _drugConceptId;
	}
	public Date getDrugExposureStartDate() {
		return _drugExposureStartDate;
	}
	public Timestamp getDrugExposureStartDatetime() {
		return _drugExposureStartDatetime;
	}
	public Date getDrugExposureEndDate() {
		return _drugExposureEndDate;
	}
	public Timestamp getDrugExposureEndDatetime() {
		return _drugExposureEndDatetime;
	}
	public Date getVerbatimEndDate() {
		return _verbatimEndDate;
	}
	public Integer getDrugTypeConceptId() {
		return _drugTypeConceptId;
	}
	public String getStopReason() {
		return _stopReason;
	}
	public Integer getRefills() {
		return _refills;
	}
	public Double getQuantity() {
		return _quantity;
	}
	public Integer getDaysSupply() {
		return _daysSupply;
	}
	public String getSig() {
		return _sig;
	}
	public Integer getRouteConceptId() {
		return _routeConceptId;
	}
	public String getLotNumber() {
		return _lotNumber;
	}
	public Long getProviderId() {
		return _providerId;
	}
	public Long getVisitOccurrenceId() {
		return _visitOccurrenceId;
	}
	public Long getVisitDetailId() {
		return _visitDetailId;
	}
	public String getDrugSourceValue() {
		return _drugSourceValue;
	}
	public Integer getDrugSourceConceptId() {
		return _drugSourceConceptId;
	}
	public String getRouteSourceValue() {
		return _routeSourceValue;
	}
	public String getDoseUnitSourceValue() {
		return _doseUnitSourceValue;
	}
	
	// POJO Setters
	// ============
	
	public void setDrugExposureId(Long value) {
		this._drugExposureId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugConceptId(Integer value) {
		this._drugConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugExposureStartDate(Date value) {
		this._drugExposureStartDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugExposureStartDatetime(Timestamp value) {
		this._drugExposureStartDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugExposureEndDate(Date value) {
		this._drugExposureEndDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugExposureEndDatetime(Timestamp value) {
		this._drugExposureEndDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setVerbatimEndDate(Date value) {
		this._verbatimEndDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugTypeConceptId(Integer value) {
		this._drugTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setStopReason(String value) {
		this._stopReason = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setRefills(Integer value) {
		this._refills = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setQuantity(Double value) {
		this._quantity = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDaysSupply(Integer value) {
		this._daysSupply = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setSig(String value) {
		this._sig = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setRouteConceptId(Integer value) {
		this._routeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setLotNumber(String value) {
		this._lotNumber = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProviderId(Long value) {
		this._providerId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setVisitOccurrenceId(Long value) {
		this._visitOccurrenceId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setVisitDetailId(Long value) {
		this._visitDetailId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugSourceValue(String value) {
		this._drugSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDrugSourceConceptId(Integer value) {
		this._drugSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setRouteSourceValue(String value) {
		this._routeSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setDoseUnitSourceValue(String value) {
		this._doseUnitSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public DrugExposureFluent withDrugExposureId(Long value) {
		setDrugExposureId(value);
		return this;
	}
	public DrugExposureFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public DrugExposureFluent withDrugConceptId(Integer value) {
		setDrugConceptId(value);
		return this;
	}
	public DrugExposureFluent withDrugExposureStartDate(Date value) {
		setDrugExposureStartDate(value);
		return this;
	}
	public DrugExposureFluent withDrugExposureStartDatetime(Timestamp value) {
		setDrugExposureStartDatetime(value);
		return this;
	}
	public DrugExposureFluent withDrugExposureEndDate(Date value) {
		setDrugExposureEndDate(value);
		return this;
	}
	public DrugExposureFluent withDrugExposureEndDatetime(Timestamp value) {
		setDrugExposureEndDatetime(value);
		return this;
	}
	public DrugExposureFluent withVerbatimEndDate(Date value) {
		setVerbatimEndDate(value);
		return this;
	}
	public DrugExposureFluent withDrugTypeConceptId(Integer value) {
		setDrugTypeConceptId(value);
		return this;
	}
	public DrugExposureFluent withStopReason(String value) {
		setStopReason(value);
		return this;
	}
	public DrugExposureFluent withRefill(Integer value) {
		setRefills(value);
		return this;
	}
	public DrugExposureFluent withQuantity(Double value) {
		setQuantity(value);
		return this;
	}
	public DrugExposureFluent withDaysSupply(Integer value) {
		setDaysSupply(value);
		return this;
	}
	public DrugExposureFluent withSig(String value) {
		setSig(value);
		return this;
	}
	public DrugExposureFluent withRouteConceptId(Integer value) {
		setRouteConceptId(value);
		return this;
	}
	public DrugExposureFluent withLotNumber(String value) {
		setLotNumber(value);
		return this;
	}
	public DrugExposureFluent withProviderId(Long value) {
		setProviderId(value);
		return this;
	}
	public DrugExposureFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public DrugExposureFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public DrugExposureFluent withDrugSourceValue(String value) {
		setDrugSourceValue(value);
		return this;
	}
	public DrugExposureFluent withDrugSourceConceptId(Integer value) {
		setDrugSourceConceptId(value);
		return this;
	}
	public DrugExposureFluent withRouteSourceValue(String value) {
		setRouteSourceValue(value);
		return this;
	}
	public DrugExposureFluent withDoseUnitSourceValue(String value) {
		setDoseUnitSourceValue(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getDrugExposureId() == null) ? 0 : getDrugExposureId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getDrugConceptId() == null) ? 0 : getDrugConceptId().hashCode());
		result = prime * result + ((getDrugExposureStartDate() == null) ? 0 : getDrugExposureStartDate().hashCode());
		result = prime * result + ((getDrugExposureStartDatetime() == null) ? 0 : getDrugExposureStartDatetime().hashCode());
		result = prime * result + ((getDrugExposureEndDate() == null) ? 0 : getDrugExposureEndDate().hashCode());
		result = prime * result + ((getDrugExposureEndDatetime() == null) ? 0 : getDrugExposureEndDatetime().hashCode());
		result = prime * result + ((getVerbatimEndDate() == null) ? 0 : getVerbatimEndDate().hashCode());
		result = prime * result + ((getDrugTypeConceptId() == null) ? 0 : getDrugTypeConceptId().hashCode());
		result = prime * result + ((getStopReason() == null) ? 0 : getStopReason().hashCode());
		result = prime * result + ((getRefills() == null) ? 0 : getRefills().hashCode());
		result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
		result = prime * result + ((getDaysSupply() == null) ? 0 : getDaysSupply().hashCode());
		result = prime * result + ((getSig() == null) ? 0 : getSig().hashCode());
		result = prime * result + ((getRouteConceptId() == null) ? 0 : getRouteConceptId().hashCode());
		result = prime * result + ((getLotNumber() == null) ? 0 : getLotNumber().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getDrugSourceValue() == null) ? 0 : getDrugSourceValue().hashCode());
		result = prime * result + ((getDrugSourceConceptId() == null) ? 0 : getDrugSourceConceptId().hashCode());
		result = prime * result + ((getRouteSourceValue() == null) ? 0 : getRouteSourceValue().hashCode());
		result = prime * result + ((getDoseUnitSourceValue() == null) ? 0 : getDoseUnitSourceValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		DrugExposureFluentImpl other = (DrugExposureFluentImpl) obj;
		//testing this.getDrugExposureId()
		if (this.getDrugExposureId() == null ^ other.getDrugExposureId()==null) return false;
		if (this.getDrugExposureId() != null && other.getDrugExposureId()!=null) {
			if (!this.getDrugExposureId().equals(other.getDrugExposureId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getDrugConceptId()
		if (this.getDrugConceptId() == null ^ other.getDrugConceptId()==null) return false;
		if (this.getDrugConceptId() != null && other.getDrugConceptId()!=null) {
			if (!this.getDrugConceptId().equals(other.getDrugConceptId())) return false;
		}
		//testing this.getDrugExposureStartDate()
		if (this.getDrugExposureStartDate() == null ^ other.getDrugExposureStartDate()==null) return false;
		if (this.getDrugExposureStartDate() != null && other.getDrugExposureStartDate()!=null) {
			if (!this.getDrugExposureStartDate().equals(other.getDrugExposureStartDate())) return false;
		}
		//testing this.getDrugExposureStartDatetime()
		if (this.getDrugExposureStartDatetime() == null ^ other.getDrugExposureStartDatetime()==null) return false;
		if (this.getDrugExposureStartDatetime() != null && other.getDrugExposureStartDatetime()!=null) {
			if (!this.getDrugExposureStartDatetime().equals(other.getDrugExposureStartDatetime())) return false;
		}
		//testing this.getDrugExposureEndDate()
		if (this.getDrugExposureEndDate() == null ^ other.getDrugExposureEndDate()==null) return false;
		if (this.getDrugExposureEndDate() != null && other.getDrugExposureEndDate()!=null) {
			if (!this.getDrugExposureEndDate().equals(other.getDrugExposureEndDate())) return false;
		}
		//testing this.getDrugExposureEndDatetime()
		if (this.getDrugExposureEndDatetime() == null ^ other.getDrugExposureEndDatetime()==null) return false;
		if (this.getDrugExposureEndDatetime() != null && other.getDrugExposureEndDatetime()!=null) {
			if (!this.getDrugExposureEndDatetime().equals(other.getDrugExposureEndDatetime())) return false;
		}
		//testing this.getVerbatimEndDate()
		if (this.getVerbatimEndDate() == null ^ other.getVerbatimEndDate()==null) return false;
		if (this.getVerbatimEndDate() != null && other.getVerbatimEndDate()!=null) {
			if (!this.getVerbatimEndDate().equals(other.getVerbatimEndDate())) return false;
		}
		//testing this.getDrugTypeConceptId()
		if (this.getDrugTypeConceptId() == null ^ other.getDrugTypeConceptId()==null) return false;
		if (this.getDrugTypeConceptId() != null && other.getDrugTypeConceptId()!=null) {
			if (!this.getDrugTypeConceptId().equals(other.getDrugTypeConceptId())) return false;
		}
		//testing this.getStopReason()
		if (this.getStopReason() == null ^ other.getStopReason()==null) return false;
		if (this.getStopReason() != null && other.getStopReason()!=null) {
			if (!this.getStopReason().equals(other.getStopReason())) return false;
		}
		//testing this.getRefills()
		if (this.getRefills() == null ^ other.getRefills()==null) return false;
		if (this.getRefills() != null && other.getRefills()!=null) {
			if (!this.getRefills().equals(other.getRefills())) return false;
		}
		//testing this.getQuantity()
		if (this.getQuantity() == null ^ other.getQuantity()==null) return false;
		if (this.getQuantity() != null && other.getQuantity()!=null) {
			if (!this.getQuantity().equals(other.getQuantity())) return false;
		}
		//testing this.getDaysSupply()
		if (this.getDaysSupply() == null ^ other.getDaysSupply()==null) return false;
		if (this.getDaysSupply() != null && other.getDaysSupply()!=null) {
			if (!this.getDaysSupply().equals(other.getDaysSupply())) return false;
		}
		//testing this.getSig()
		if (this.getSig() == null ^ other.getSig()==null) return false;
		if (this.getSig() != null && other.getSig()!=null) {
			if (!this.getSig().equals(other.getSig())) return false;
		}
		//testing this.getRouteConceptId()
		if (this.getRouteConceptId() == null ^ other.getRouteConceptId()==null) return false;
		if (this.getRouteConceptId() != null && other.getRouteConceptId()!=null) {
			if (!this.getRouteConceptId().equals(other.getRouteConceptId())) return false;
		}
		//testing this.getLotNumber()
		if (this.getLotNumber() == null ^ other.getLotNumber()==null) return false;
		if (this.getLotNumber() != null && other.getLotNumber()!=null) {
			if (!this.getLotNumber().equals(other.getLotNumber())) return false;
		}
		//testing this.getProviderId()
		if (this.getProviderId() == null ^ other.getProviderId()==null) return false;
		if (this.getProviderId() != null && other.getProviderId()!=null) {
			if (!this.getProviderId().equals(other.getProviderId())) return false;
		}
		//testing this.getVisitOccurrenceId()
		if (this.getVisitOccurrenceId() == null ^ other.getVisitOccurrenceId()==null) return false;
		if (this.getVisitOccurrenceId() != null && other.getVisitOccurrenceId()!=null) {
			if (!this.getVisitOccurrenceId().equals(other.getVisitOccurrenceId())) return false;
		}
		//testing this.getVisitDetailId()
		if (this.getVisitDetailId() == null ^ other.getVisitDetailId()==null) return false;
		if (this.getVisitDetailId() != null && other.getVisitDetailId()!=null) {
			if (!this.getVisitDetailId().equals(other.getVisitDetailId())) return false;
		}
		//testing this.getDrugSourceValue()
		if (this.getDrugSourceValue() == null ^ other.getDrugSourceValue()==null) return false;
		if (this.getDrugSourceValue() != null && other.getDrugSourceValue()!=null) {
			if (!this.getDrugSourceValue().equals(other.getDrugSourceValue())) return false;
		}
		//testing this.getDrugSourceConceptId()
		if (this.getDrugSourceConceptId() == null ^ other.getDrugSourceConceptId()==null) return false;
		if (this.getDrugSourceConceptId() != null && other.getDrugSourceConceptId()!=null) {
			if (!this.getDrugSourceConceptId().equals(other.getDrugSourceConceptId())) return false;
		}
		//testing this.getRouteSourceValue()
		if (this.getRouteSourceValue() == null ^ other.getRouteSourceValue()==null) return false;
		if (this.getRouteSourceValue() != null && other.getRouteSourceValue()!=null) {
			if (!this.getRouteSourceValue().equals(other.getRouteSourceValue())) return false;
		}
		//testing this.getDoseUnitSourceValue()
		if (this.getDoseUnitSourceValue() == null ^ other.getDoseUnitSourceValue()==null) return false;
		if (this.getDoseUnitSourceValue() != null && other.getDoseUnitSourceValue()!=null) {
			if (!this.getDoseUnitSourceValue().equals(other.getDoseUnitSourceValue())) return false;
		}
		return true;
	}
	
}
