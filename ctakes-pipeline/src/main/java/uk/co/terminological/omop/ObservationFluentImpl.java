package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.ObservationFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ObservationFluentImpl extends Observable implements Observation, ObservationFluent  {

	public static ObservationFluent create() {
		return new ObservationFluentImpl();
	}

	// Fields
	// ======

	private Long _observationId;
	private Long _personId;
	private Integer _observationConceptId;
	private Date _observationDate;
	private Timestamp _observationDatetime;
	private Integer _observationTypeConceptId;
	private Double _valueAsNumber;
	private String _valueAsString;
	private Integer _valueAsConceptId;
	private Integer _qualifierConceptId;
	private Integer _unitConceptId;
	private Integer _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _observationSourceValue;
	private Integer _observationSourceConceptId;
	private String _unitSourceValue;
	private String _qualifierSourceValue;
	private Long _observationEventId;
	private Integer _obsEventFieldConceptId;
	private Timestamp _valueAsDatetime;

	// Public constructor
	// ==================

	public ObservationFluentImpl() {}

	public ObservationFluentImpl(
		Long _observationId,
		Long _personId,
		Integer _observationConceptId,
		Date _observationDate,
		Timestamp _observationDatetime,
		Integer _observationTypeConceptId,
		Double _valueAsNumber,
		String _valueAsString,
		Integer _valueAsConceptId,
		Integer _qualifierConceptId,
		Integer _unitConceptId,
		Integer _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _observationSourceValue,
		Integer _observationSourceConceptId,
		String _unitSourceValue,
		String _qualifierSourceValue,
		Long _observationEventId,
		Integer _obsEventFieldConceptId,
		Timestamp _valueAsDatetime
	) {
		this._observationId = _observationId;
		this._personId = _personId;
		this._observationConceptId = _observationConceptId;
		this._observationDate = _observationDate;
		this._observationDatetime = _observationDatetime;
		this._observationTypeConceptId = _observationTypeConceptId;
		this._valueAsNumber = _valueAsNumber;
		this._valueAsString = _valueAsString;
		this._valueAsConceptId = _valueAsConceptId;
		this._qualifierConceptId = _qualifierConceptId;
		this._unitConceptId = _unitConceptId;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._observationSourceValue = _observationSourceValue;
		this._observationSourceConceptId = _observationSourceConceptId;
		this._unitSourceValue = _unitSourceValue;
		this._qualifierSourceValue = _qualifierSourceValue;
		this._observationEventId = _observationEventId;
		this._obsEventFieldConceptId = _obsEventFieldConceptId;
		this._valueAsDatetime = _valueAsDatetime;
	}
	
	public ObservationFluentImpl(Observation clone) {
		this._observationId = clone.getObservationId();
		this._personId = clone.getPersonId();
		this._observationConceptId = clone.getObservationConceptId();
		this._observationDate = (Date) clone.getObservationDate().clone();
		this._observationDatetime = (Timestamp) clone.getObservationDatetime().clone();
		this._observationTypeConceptId = clone.getObservationTypeConceptId();
		this._valueAsNumber = clone.getValueAsNumber();
		this._valueAsString = clone.getValueAsString();
		this._valueAsConceptId = clone.getValueAsConceptId();
		this._qualifierConceptId = clone.getQualifierConceptId();
		this._unitConceptId = clone.getUnitConceptId();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._observationSourceValue = clone.getObservationSourceValue();
		this._observationSourceConceptId = clone.getObservationSourceConceptId();
		this._unitSourceValue = clone.getUnitSourceValue();
		this._qualifierSourceValue = clone.getQualifierSourceValue();
		this._observationEventId = clone.getObservationEventId();
		this._obsEventFieldConceptId = clone.getObsEventFieldConceptId();
		this._valueAsDatetime = (Timestamp) clone.getValueAsDatetime().clone();
	}
	
	public ObservationFluentImpl clone() {
		return new ObservationFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getObservationId() {
		return _observationId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Integer getObservationConceptId() {
		return _observationConceptId;
	}
	public Date getObservationDate() {
		return _observationDate;
	}
	public Timestamp getObservationDatetime() {
		return _observationDatetime;
	}
	public Integer getObservationTypeConceptId() {
		return _observationTypeConceptId;
	}
	public Double getValueAsNumber() {
		return _valueAsNumber;
	}
	public String getValueAsString() {
		return _valueAsString;
	}
	public Integer getValueAsConceptId() {
		return _valueAsConceptId;
	}
	public Integer getQualifierConceptId() {
		return _qualifierConceptId;
	}
	public Integer getUnitConceptId() {
		return _unitConceptId;
	}
	public Integer getProviderId() {
		return _providerId;
	}
	public Long getVisitOccurrenceId() {
		return _visitOccurrenceId;
	}
	public Long getVisitDetailId() {
		return _visitDetailId;
	}
	public String getObservationSourceValue() {
		return _observationSourceValue;
	}
	public Integer getObservationSourceConceptId() {
		return _observationSourceConceptId;
	}
	public String getUnitSourceValue() {
		return _unitSourceValue;
	}
	public String getQualifierSourceValue() {
		return _qualifierSourceValue;
	}
	public Long getObservationEventId() {
		return _observationEventId;
	}
	public Integer getObsEventFieldConceptId() {
		return _obsEventFieldConceptId;
	}
	public Timestamp getValueAsDatetime() {
		return _valueAsDatetime;
	}
	
	// POJO Setters
	// ============
	
	public void setObservationId(Long value) {
		this._observationId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationConceptId(Integer value) {
		this._observationConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationDate(Date value) {
		this._observationDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationDatetime(Timestamp value) {
		this._observationDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationTypeConceptId(Integer value) {
		this._observationTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsNumber(Double value) {
		this._valueAsNumber = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsString(String value) {
		this._valueAsString = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsConceptId(Integer value) {
		this._valueAsConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setQualifierConceptId(Integer value) {
		this._qualifierConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setUnitConceptId(Integer value) {
		this._unitConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProviderId(Integer value) {
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
	
	public void setObservationSourceValue(String value) {
		this._observationSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationSourceConceptId(Integer value) {
		this._observationSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setUnitSourceValue(String value) {
		this._unitSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setQualifierSourceValue(String value) {
		this._qualifierSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObservationEventId(Long value) {
		this._observationEventId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setObsEventFieldConceptId(Integer value) {
		this._obsEventFieldConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsDatetime(Timestamp value) {
		this._valueAsDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public ObservationFluent withObservationId(Long value) {
		setObservationId(value);
		return this;
	}
	public ObservationFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public ObservationFluent withObservationConceptId(Integer value) {
		setObservationConceptId(value);
		return this;
	}
	public ObservationFluent withObservationDate(Date value) {
		setObservationDate(value);
		return this;
	}
	public ObservationFluent withObservationDatetime(Timestamp value) {
		setObservationDatetime(value);
		return this;
	}
	public ObservationFluent withObservationTypeConceptId(Integer value) {
		setObservationTypeConceptId(value);
		return this;
	}
	public ObservationFluent withValueAsNumber(Double value) {
		setValueAsNumber(value);
		return this;
	}
	public ObservationFluent withValueAsString(String value) {
		setValueAsString(value);
		return this;
	}
	public ObservationFluent withValueAsConceptId(Integer value) {
		setValueAsConceptId(value);
		return this;
	}
	public ObservationFluent withQualifierConceptId(Integer value) {
		setQualifierConceptId(value);
		return this;
	}
	public ObservationFluent withUnitConceptId(Integer value) {
		setUnitConceptId(value);
		return this;
	}
	public ObservationFluent withProviderId(Integer value) {
		setProviderId(value);
		return this;
	}
	public ObservationFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public ObservationFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public ObservationFluent withObservationSourceValue(String value) {
		setObservationSourceValue(value);
		return this;
	}
	public ObservationFluent withObservationSourceConceptId(Integer value) {
		setObservationSourceConceptId(value);
		return this;
	}
	public ObservationFluent withUnitSourceValue(String value) {
		setUnitSourceValue(value);
		return this;
	}
	public ObservationFluent withQualifierSourceValue(String value) {
		setQualifierSourceValue(value);
		return this;
	}
	public ObservationFluent withObservationEventId(Long value) {
		setObservationEventId(value);
		return this;
	}
	public ObservationFluent withObsEventFieldConceptId(Integer value) {
		setObsEventFieldConceptId(value);
		return this;
	}
	public ObservationFluent withValueAsDatetime(Timestamp value) {
		setValueAsDatetime(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getObservationId() == null) ? 0 : getObservationId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getObservationConceptId() == null) ? 0 : getObservationConceptId().hashCode());
		result = prime * result + ((getObservationDate() == null) ? 0 : getObservationDate().hashCode());
		result = prime * result + ((getObservationDatetime() == null) ? 0 : getObservationDatetime().hashCode());
		result = prime * result + ((getObservationTypeConceptId() == null) ? 0 : getObservationTypeConceptId().hashCode());
		result = prime * result + ((getValueAsNumber() == null) ? 0 : getValueAsNumber().hashCode());
		result = prime * result + ((getValueAsString() == null) ? 0 : getValueAsString().hashCode());
		result = prime * result + ((getValueAsConceptId() == null) ? 0 : getValueAsConceptId().hashCode());
		result = prime * result + ((getQualifierConceptId() == null) ? 0 : getQualifierConceptId().hashCode());
		result = prime * result + ((getUnitConceptId() == null) ? 0 : getUnitConceptId().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getObservationSourceValue() == null) ? 0 : getObservationSourceValue().hashCode());
		result = prime * result + ((getObservationSourceConceptId() == null) ? 0 : getObservationSourceConceptId().hashCode());
		result = prime * result + ((getUnitSourceValue() == null) ? 0 : getUnitSourceValue().hashCode());
		result = prime * result + ((getQualifierSourceValue() == null) ? 0 : getQualifierSourceValue().hashCode());
		result = prime * result + ((getObservationEventId() == null) ? 0 : getObservationEventId().hashCode());
		result = prime * result + ((getObsEventFieldConceptId() == null) ? 0 : getObsEventFieldConceptId().hashCode());
		result = prime * result + ((getValueAsDatetime() == null) ? 0 : getValueAsDatetime().hashCode());
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
		ObservationFluentImpl other = (ObservationFluentImpl) obj;
		//testing this.getObservationId()
		if (this.getObservationId() == null ^ other.getObservationId()==null) return false;
		if (this.getObservationId() != null && other.getObservationId()!=null) {
			if (!this.getObservationId().equals(other.getObservationId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getObservationConceptId()
		if (this.getObservationConceptId() == null ^ other.getObservationConceptId()==null) return false;
		if (this.getObservationConceptId() != null && other.getObservationConceptId()!=null) {
			if (!this.getObservationConceptId().equals(other.getObservationConceptId())) return false;
		}
		//testing this.getObservationDate()
		if (this.getObservationDate() == null ^ other.getObservationDate()==null) return false;
		if (this.getObservationDate() != null && other.getObservationDate()!=null) {
			if (!this.getObservationDate().equals(other.getObservationDate())) return false;
		}
		//testing this.getObservationDatetime()
		if (this.getObservationDatetime() == null ^ other.getObservationDatetime()==null) return false;
		if (this.getObservationDatetime() != null && other.getObservationDatetime()!=null) {
			if (!this.getObservationDatetime().equals(other.getObservationDatetime())) return false;
		}
		//testing this.getObservationTypeConceptId()
		if (this.getObservationTypeConceptId() == null ^ other.getObservationTypeConceptId()==null) return false;
		if (this.getObservationTypeConceptId() != null && other.getObservationTypeConceptId()!=null) {
			if (!this.getObservationTypeConceptId().equals(other.getObservationTypeConceptId())) return false;
		}
		//testing this.getValueAsNumber()
		if (this.getValueAsNumber() == null ^ other.getValueAsNumber()==null) return false;
		if (this.getValueAsNumber() != null && other.getValueAsNumber()!=null) {
			if (!this.getValueAsNumber().equals(other.getValueAsNumber())) return false;
		}
		//testing this.getValueAsString()
		if (this.getValueAsString() == null ^ other.getValueAsString()==null) return false;
		if (this.getValueAsString() != null && other.getValueAsString()!=null) {
			if (!this.getValueAsString().equals(other.getValueAsString())) return false;
		}
		//testing this.getValueAsConceptId()
		if (this.getValueAsConceptId() == null ^ other.getValueAsConceptId()==null) return false;
		if (this.getValueAsConceptId() != null && other.getValueAsConceptId()!=null) {
			if (!this.getValueAsConceptId().equals(other.getValueAsConceptId())) return false;
		}
		//testing this.getQualifierConceptId()
		if (this.getQualifierConceptId() == null ^ other.getQualifierConceptId()==null) return false;
		if (this.getQualifierConceptId() != null && other.getQualifierConceptId()!=null) {
			if (!this.getQualifierConceptId().equals(other.getQualifierConceptId())) return false;
		}
		//testing this.getUnitConceptId()
		if (this.getUnitConceptId() == null ^ other.getUnitConceptId()==null) return false;
		if (this.getUnitConceptId() != null && other.getUnitConceptId()!=null) {
			if (!this.getUnitConceptId().equals(other.getUnitConceptId())) return false;
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
		//testing this.getObservationSourceValue()
		if (this.getObservationSourceValue() == null ^ other.getObservationSourceValue()==null) return false;
		if (this.getObservationSourceValue() != null && other.getObservationSourceValue()!=null) {
			if (!this.getObservationSourceValue().equals(other.getObservationSourceValue())) return false;
		}
		//testing this.getObservationSourceConceptId()
		if (this.getObservationSourceConceptId() == null ^ other.getObservationSourceConceptId()==null) return false;
		if (this.getObservationSourceConceptId() != null && other.getObservationSourceConceptId()!=null) {
			if (!this.getObservationSourceConceptId().equals(other.getObservationSourceConceptId())) return false;
		}
		//testing this.getUnitSourceValue()
		if (this.getUnitSourceValue() == null ^ other.getUnitSourceValue()==null) return false;
		if (this.getUnitSourceValue() != null && other.getUnitSourceValue()!=null) {
			if (!this.getUnitSourceValue().equals(other.getUnitSourceValue())) return false;
		}
		//testing this.getQualifierSourceValue()
		if (this.getQualifierSourceValue() == null ^ other.getQualifierSourceValue()==null) return false;
		if (this.getQualifierSourceValue() != null && other.getQualifierSourceValue()!=null) {
			if (!this.getQualifierSourceValue().equals(other.getQualifierSourceValue())) return false;
		}
		//testing this.getObservationEventId()
		if (this.getObservationEventId() == null ^ other.getObservationEventId()==null) return false;
		if (this.getObservationEventId() != null && other.getObservationEventId()!=null) {
			if (!this.getObservationEventId().equals(other.getObservationEventId())) return false;
		}
		//testing this.getObsEventFieldConceptId()
		if (this.getObsEventFieldConceptId() == null ^ other.getObsEventFieldConceptId()==null) return false;
		if (this.getObsEventFieldConceptId() != null && other.getObsEventFieldConceptId()!=null) {
			if (!this.getObsEventFieldConceptId().equals(other.getObsEventFieldConceptId())) return false;
		}
		//testing this.getValueAsDatetime()
		if (this.getValueAsDatetime() == null ^ other.getValueAsDatetime()==null) return false;
		if (this.getValueAsDatetime() != null && other.getValueAsDatetime()!=null) {
			if (!this.getValueAsDatetime().equals(other.getValueAsDatetime())) return false;
		}
		return true;
	}
	
}
