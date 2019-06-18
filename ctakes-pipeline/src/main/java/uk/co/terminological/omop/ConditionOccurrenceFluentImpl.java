package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.ConditionOccurrenceFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ConditionOccurrenceFluentImpl extends Observable implements ConditionOccurrence, ConditionOccurrenceFluent  {

	public static ConditionOccurrenceFluent create() {
		return new ConditionOccurrenceFluentImpl();
	}

	// Fields
	// ======

	private Long _conditionOccurrenceId;
	private Long _personId;
	private Integer _conditionConceptId;
	private Date _conditionStartDate;
	private Timestamp _conditionStartDatetime;
	private Date _conditionEndDate;
	private Timestamp _conditionEndDatetime;
	private Integer _conditionTypeConceptId;
	private Integer _conditionStatusConceptId;
	private String _stopReason;
	private Long _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _conditionSourceValue;
	private Integer _conditionSourceConceptId;
	private String _conditionStatusSourceValue;

	// Public constructor
	// ==================

	public ConditionOccurrenceFluentImpl() {}

	public ConditionOccurrenceFluentImpl(
		Long _conditionOccurrenceId,
		Long _personId,
		Integer _conditionConceptId,
		Date _conditionStartDate,
		Timestamp _conditionStartDatetime,
		Date _conditionEndDate,
		Timestamp _conditionEndDatetime,
		Integer _conditionTypeConceptId,
		Integer _conditionStatusConceptId,
		String _stopReason,
		Long _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _conditionSourceValue,
		Integer _conditionSourceConceptId,
		String _conditionStatusSourceValue
	) {
		this._conditionOccurrenceId = _conditionOccurrenceId;
		this._personId = _personId;
		this._conditionConceptId = _conditionConceptId;
		this._conditionStartDate = _conditionStartDate;
		this._conditionStartDatetime = _conditionStartDatetime;
		this._conditionEndDate = _conditionEndDate;
		this._conditionEndDatetime = _conditionEndDatetime;
		this._conditionTypeConceptId = _conditionTypeConceptId;
		this._conditionStatusConceptId = _conditionStatusConceptId;
		this._stopReason = _stopReason;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._conditionSourceValue = _conditionSourceValue;
		this._conditionSourceConceptId = _conditionSourceConceptId;
		this._conditionStatusSourceValue = _conditionStatusSourceValue;
	}
	
	public ConditionOccurrenceFluentImpl(ConditionOccurrence clone) {
		this._conditionOccurrenceId = clone.getConditionOccurrenceId();
		this._personId = clone.getPersonId();
		this._conditionConceptId = clone.getConditionConceptId();
		this._conditionStartDate = (Date) clone.getConditionStartDate().clone();
		this._conditionStartDatetime = (Timestamp) clone.getConditionStartDatetime().clone();
		this._conditionEndDate = (Date) clone.getConditionEndDate().clone();
		this._conditionEndDatetime = (Timestamp) clone.getConditionEndDatetime().clone();
		this._conditionTypeConceptId = clone.getConditionTypeConceptId();
		this._conditionStatusConceptId = clone.getConditionStatusConceptId();
		this._stopReason = clone.getStopReason();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._conditionSourceValue = clone.getConditionSourceValue();
		this._conditionSourceConceptId = clone.getConditionSourceConceptId();
		this._conditionStatusSourceValue = clone.getConditionStatusSourceValue();
	}
	
	public ConditionOccurrenceFluentImpl clone() {
		return new ConditionOccurrenceFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getConditionOccurrenceId() {
		return _conditionOccurrenceId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Integer getConditionConceptId() {
		return _conditionConceptId;
	}
	public Date getConditionStartDate() {
		return _conditionStartDate;
	}
	public Timestamp getConditionStartDatetime() {
		return _conditionStartDatetime;
	}
	public Date getConditionEndDate() {
		return _conditionEndDate;
	}
	public Timestamp getConditionEndDatetime() {
		return _conditionEndDatetime;
	}
	public Integer getConditionTypeConceptId() {
		return _conditionTypeConceptId;
	}
	public Integer getConditionStatusConceptId() {
		return _conditionStatusConceptId;
	}
	public String getStopReason() {
		return _stopReason;
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
	public String getConditionSourceValue() {
		return _conditionSourceValue;
	}
	public Integer getConditionSourceConceptId() {
		return _conditionSourceConceptId;
	}
	public String getConditionStatusSourceValue() {
		return _conditionStatusSourceValue;
	}
	
	// POJO Setters
	// ============
	
	public void setConditionOccurrenceId(Long value) {
		this._conditionOccurrenceId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionConceptId(Integer value) {
		this._conditionConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionStartDate(Date value) {
		this._conditionStartDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionStartDatetime(Timestamp value) {
		this._conditionStartDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionEndDate(Date value) {
		this._conditionEndDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionEndDatetime(Timestamp value) {
		this._conditionEndDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionTypeConceptId(Integer value) {
		this._conditionTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionStatusConceptId(Integer value) {
		this._conditionStatusConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setStopReason(String value) {
		this._stopReason = value;
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
	
	public void setConditionSourceValue(String value) {
		this._conditionSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionSourceConceptId(Integer value) {
		this._conditionSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConditionStatusSourceValue(String value) {
		this._conditionStatusSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public ConditionOccurrenceFluent withConditionOccurrenceId(Long value) {
		setConditionOccurrenceId(value);
		return this;
	}
	public ConditionOccurrenceFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionConceptId(Integer value) {
		setConditionConceptId(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionStartDate(Date value) {
		setConditionStartDate(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionStartDatetime(Timestamp value) {
		setConditionStartDatetime(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionEndDate(Date value) {
		setConditionEndDate(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionEndDatetime(Timestamp value) {
		setConditionEndDatetime(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionTypeConceptId(Integer value) {
		setConditionTypeConceptId(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionStatusConceptId(Integer value) {
		setConditionStatusConceptId(value);
		return this;
	}
	public ConditionOccurrenceFluent withStopReason(String value) {
		setStopReason(value);
		return this;
	}
	public ConditionOccurrenceFluent withProviderId(Long value) {
		setProviderId(value);
		return this;
	}
	public ConditionOccurrenceFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public ConditionOccurrenceFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionSourceValue(String value) {
		setConditionSourceValue(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionSourceConceptId(Integer value) {
		setConditionSourceConceptId(value);
		return this;
	}
	public ConditionOccurrenceFluent withConditionStatusSourceValue(String value) {
		setConditionStatusSourceValue(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getConditionOccurrenceId() == null) ? 0 : getConditionOccurrenceId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getConditionConceptId() == null) ? 0 : getConditionConceptId().hashCode());
		result = prime * result + ((getConditionStartDate() == null) ? 0 : getConditionStartDate().hashCode());
		result = prime * result + ((getConditionStartDatetime() == null) ? 0 : getConditionStartDatetime().hashCode());
		result = prime * result + ((getConditionEndDate() == null) ? 0 : getConditionEndDate().hashCode());
		result = prime * result + ((getConditionEndDatetime() == null) ? 0 : getConditionEndDatetime().hashCode());
		result = prime * result + ((getConditionTypeConceptId() == null) ? 0 : getConditionTypeConceptId().hashCode());
		result = prime * result + ((getConditionStatusConceptId() == null) ? 0 : getConditionStatusConceptId().hashCode());
		result = prime * result + ((getStopReason() == null) ? 0 : getStopReason().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getConditionSourceValue() == null) ? 0 : getConditionSourceValue().hashCode());
		result = prime * result + ((getConditionSourceConceptId() == null) ? 0 : getConditionSourceConceptId().hashCode());
		result = prime * result + ((getConditionStatusSourceValue() == null) ? 0 : getConditionStatusSourceValue().hashCode());
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
		ConditionOccurrenceFluentImpl other = (ConditionOccurrenceFluentImpl) obj;
		//testing this.getConditionOccurrenceId()
		if (this.getConditionOccurrenceId() == null ^ other.getConditionOccurrenceId()==null) return false;
		if (this.getConditionOccurrenceId() != null && other.getConditionOccurrenceId()!=null) {
			if (!this.getConditionOccurrenceId().equals(other.getConditionOccurrenceId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getConditionConceptId()
		if (this.getConditionConceptId() == null ^ other.getConditionConceptId()==null) return false;
		if (this.getConditionConceptId() != null && other.getConditionConceptId()!=null) {
			if (!this.getConditionConceptId().equals(other.getConditionConceptId())) return false;
		}
		//testing this.getConditionStartDate()
		if (this.getConditionStartDate() == null ^ other.getConditionStartDate()==null) return false;
		if (this.getConditionStartDate() != null && other.getConditionStartDate()!=null) {
			if (!this.getConditionStartDate().equals(other.getConditionStartDate())) return false;
		}
		//testing this.getConditionStartDatetime()
		if (this.getConditionStartDatetime() == null ^ other.getConditionStartDatetime()==null) return false;
		if (this.getConditionStartDatetime() != null && other.getConditionStartDatetime()!=null) {
			if (!this.getConditionStartDatetime().equals(other.getConditionStartDatetime())) return false;
		}
		//testing this.getConditionEndDate()
		if (this.getConditionEndDate() == null ^ other.getConditionEndDate()==null) return false;
		if (this.getConditionEndDate() != null && other.getConditionEndDate()!=null) {
			if (!this.getConditionEndDate().equals(other.getConditionEndDate())) return false;
		}
		//testing this.getConditionEndDatetime()
		if (this.getConditionEndDatetime() == null ^ other.getConditionEndDatetime()==null) return false;
		if (this.getConditionEndDatetime() != null && other.getConditionEndDatetime()!=null) {
			if (!this.getConditionEndDatetime().equals(other.getConditionEndDatetime())) return false;
		}
		//testing this.getConditionTypeConceptId()
		if (this.getConditionTypeConceptId() == null ^ other.getConditionTypeConceptId()==null) return false;
		if (this.getConditionTypeConceptId() != null && other.getConditionTypeConceptId()!=null) {
			if (!this.getConditionTypeConceptId().equals(other.getConditionTypeConceptId())) return false;
		}
		//testing this.getConditionStatusConceptId()
		if (this.getConditionStatusConceptId() == null ^ other.getConditionStatusConceptId()==null) return false;
		if (this.getConditionStatusConceptId() != null && other.getConditionStatusConceptId()!=null) {
			if (!this.getConditionStatusConceptId().equals(other.getConditionStatusConceptId())) return false;
		}
		//testing this.getStopReason()
		if (this.getStopReason() == null ^ other.getStopReason()==null) return false;
		if (this.getStopReason() != null && other.getStopReason()!=null) {
			if (!this.getStopReason().equals(other.getStopReason())) return false;
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
		//testing this.getConditionSourceValue()
		if (this.getConditionSourceValue() == null ^ other.getConditionSourceValue()==null) return false;
		if (this.getConditionSourceValue() != null && other.getConditionSourceValue()!=null) {
			if (!this.getConditionSourceValue().equals(other.getConditionSourceValue())) return false;
		}
		//testing this.getConditionSourceConceptId()
		if (this.getConditionSourceConceptId() == null ^ other.getConditionSourceConceptId()==null) return false;
		if (this.getConditionSourceConceptId() != null && other.getConditionSourceConceptId()!=null) {
			if (!this.getConditionSourceConceptId().equals(other.getConditionSourceConceptId())) return false;
		}
		//testing this.getConditionStatusSourceValue()
		if (this.getConditionStatusSourceValue() == null ^ other.getConditionStatusSourceValue()==null) return false;
		if (this.getConditionStatusSourceValue() != null && other.getConditionStatusSourceValue()!=null) {
			if (!this.getConditionStatusSourceValue().equals(other.getConditionStatusSourceValue())) return false;
		}
		return true;
	}
	
}
