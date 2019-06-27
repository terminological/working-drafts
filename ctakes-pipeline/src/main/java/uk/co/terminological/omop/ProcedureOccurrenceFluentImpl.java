package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.ProcedureOccurrenceFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ProcedureOccurrenceFluentImpl extends Observable implements ProcedureOccurrence, ProcedureOccurrenceFluent  {

	public static ProcedureOccurrenceFluent create() {
		return new ProcedureOccurrenceFluentImpl();
	}

	// Fields
	// ======

	private Long _procedureOccurrenceId;
	private Long _personId;
	private Integer _procedureConceptId;
	private Date _procedureDate;
	private Timestamp _procedureDatetime;
	private Integer _procedureTypeConceptId;
	private Integer _modifierConceptId;
	private Integer _quantity;
	private Long _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _procedureSourceValue;
	private Integer _procedureSourceConceptId;
	private String _modifierSourceValue;

	// Public constructor
	// ==================

	public ProcedureOccurrenceFluentImpl() {}

	public ProcedureOccurrenceFluentImpl(
		Long _procedureOccurrenceId,
		Long _personId,
		Integer _procedureConceptId,
		Date _procedureDate,
		Timestamp _procedureDatetime,
		Integer _procedureTypeConceptId,
		Integer _modifierConceptId,
		Integer _quantity,
		Long _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _procedureSourceValue,
		Integer _procedureSourceConceptId,
		String _modifierSourceValue
	) {
		this._procedureOccurrenceId = _procedureOccurrenceId;
		this._personId = _personId;
		this._procedureConceptId = _procedureConceptId;
		this._procedureDate = _procedureDate;
		this._procedureDatetime = _procedureDatetime;
		this._procedureTypeConceptId = _procedureTypeConceptId;
		this._modifierConceptId = _modifierConceptId;
		this._quantity = _quantity;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._procedureSourceValue = _procedureSourceValue;
		this._procedureSourceConceptId = _procedureSourceConceptId;
		this._modifierSourceValue = _modifierSourceValue;
	}
	
	public ProcedureOccurrenceFluentImpl(ProcedureOccurrence clone) {
		this._procedureOccurrenceId = clone.getProcedureOccurrenceId();
		this._personId = clone.getPersonId();
		this._procedureConceptId = clone.getProcedureConceptId();
		this._procedureDate = (Date) clone.getProcedureDate().clone();
		this._procedureDatetime = (Timestamp) clone.getProcedureDatetime().clone();
		this._procedureTypeConceptId = clone.getProcedureTypeConceptId();
		this._modifierConceptId = clone.getModifierConceptId();
		this._quantity = clone.getQuantity();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._procedureSourceValue = clone.getProcedureSourceValue();
		this._procedureSourceConceptId = clone.getProcedureSourceConceptId();
		this._modifierSourceValue = clone.getModifierSourceValue();
	}
	
	public ProcedureOccurrenceFluentImpl clone() {
		return new ProcedureOccurrenceFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getProcedureOccurrenceId() {
		return _procedureOccurrenceId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Integer getProcedureConceptId() {
		return _procedureConceptId;
	}
	public Date getProcedureDate() {
		return _procedureDate;
	}
	public Timestamp getProcedureDatetime() {
		return _procedureDatetime;
	}
	public Integer getProcedureTypeConceptId() {
		return _procedureTypeConceptId;
	}
	public Integer getModifierConceptId() {
		return _modifierConceptId;
	}
	public Integer getQuantity() {
		return _quantity;
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
	public String getProcedureSourceValue() {
		return _procedureSourceValue;
	}
	public Integer getProcedureSourceConceptId() {
		return _procedureSourceConceptId;
	}
	public String getModifierSourceValue() {
		return _modifierSourceValue;
	}
	
	// POJO Setters
	// ============
	
	public void setProcedureOccurrenceId(Long value) {
		this._procedureOccurrenceId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProcedureConceptId(Integer value) {
		this._procedureConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProcedureDate(Date value) {
		this._procedureDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProcedureDatetime(Timestamp value) {
		this._procedureDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProcedureTypeConceptId(Integer value) {
		this._procedureTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setModifierConceptId(Integer value) {
		this._modifierConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setQuantity(Integer value) {
		this._quantity = value;
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
	
	public void setProcedureSourceValue(String value) {
		this._procedureSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setProcedureSourceConceptId(Integer value) {
		this._procedureSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setModifierSourceValue(String value) {
		this._modifierSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public ProcedureOccurrenceFluent withProcedureOccurrenceId(Long value) {
		setProcedureOccurrenceId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureConceptId(Integer value) {
		setProcedureConceptId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureDate(Date value) {
		setProcedureDate(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureDatetime(Timestamp value) {
		setProcedureDatetime(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureTypeConceptId(Integer value) {
		setProcedureTypeConceptId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withModifierConceptId(Integer value) {
		setModifierConceptId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withQuantity(Integer value) {
		setQuantity(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProviderId(Long value) {
		setProviderId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureSourceValue(String value) {
		setProcedureSourceValue(value);
		return this;
	}
	public ProcedureOccurrenceFluent withProcedureSourceConceptId(Integer value) {
		setProcedureSourceConceptId(value);
		return this;
	}
	public ProcedureOccurrenceFluent withModifierSourceValue(String value) {
		setModifierSourceValue(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getProcedureOccurrenceId() == null) ? 0 : getProcedureOccurrenceId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getProcedureConceptId() == null) ? 0 : getProcedureConceptId().hashCode());
		result = prime * result + ((getProcedureDate() == null) ? 0 : getProcedureDate().hashCode());
		result = prime * result + ((getProcedureDatetime() == null) ? 0 : getProcedureDatetime().hashCode());
		result = prime * result + ((getProcedureTypeConceptId() == null) ? 0 : getProcedureTypeConceptId().hashCode());
		result = prime * result + ((getModifierConceptId() == null) ? 0 : getModifierConceptId().hashCode());
		result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getProcedureSourceValue() == null) ? 0 : getProcedureSourceValue().hashCode());
		result = prime * result + ((getProcedureSourceConceptId() == null) ? 0 : getProcedureSourceConceptId().hashCode());
		result = prime * result + ((getModifierSourceValue() == null) ? 0 : getModifierSourceValue().hashCode());
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
		ProcedureOccurrenceFluentImpl other = (ProcedureOccurrenceFluentImpl) obj;
		//testing this.getProcedureOccurrenceId()
		if (this.getProcedureOccurrenceId() == null ^ other.getProcedureOccurrenceId()==null) return false;
		if (this.getProcedureOccurrenceId() != null && other.getProcedureOccurrenceId()!=null) {
			if (!this.getProcedureOccurrenceId().equals(other.getProcedureOccurrenceId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getProcedureConceptId()
		if (this.getProcedureConceptId() == null ^ other.getProcedureConceptId()==null) return false;
		if (this.getProcedureConceptId() != null && other.getProcedureConceptId()!=null) {
			if (!this.getProcedureConceptId().equals(other.getProcedureConceptId())) return false;
		}
		//testing this.getProcedureDate()
		if (this.getProcedureDate() == null ^ other.getProcedureDate()==null) return false;
		if (this.getProcedureDate() != null && other.getProcedureDate()!=null) {
			if (!this.getProcedureDate().equals(other.getProcedureDate())) return false;
		}
		//testing this.getProcedureDatetime()
		if (this.getProcedureDatetime() == null ^ other.getProcedureDatetime()==null) return false;
		if (this.getProcedureDatetime() != null && other.getProcedureDatetime()!=null) {
			if (!this.getProcedureDatetime().equals(other.getProcedureDatetime())) return false;
		}
		//testing this.getProcedureTypeConceptId()
		if (this.getProcedureTypeConceptId() == null ^ other.getProcedureTypeConceptId()==null) return false;
		if (this.getProcedureTypeConceptId() != null && other.getProcedureTypeConceptId()!=null) {
			if (!this.getProcedureTypeConceptId().equals(other.getProcedureTypeConceptId())) return false;
		}
		//testing this.getModifierConceptId()
		if (this.getModifierConceptId() == null ^ other.getModifierConceptId()==null) return false;
		if (this.getModifierConceptId() != null && other.getModifierConceptId()!=null) {
			if (!this.getModifierConceptId().equals(other.getModifierConceptId())) return false;
		}
		//testing this.getQuantity()
		if (this.getQuantity() == null ^ other.getQuantity()==null) return false;
		if (this.getQuantity() != null && other.getQuantity()!=null) {
			if (!this.getQuantity().equals(other.getQuantity())) return false;
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
		//testing this.getProcedureSourceValue()
		if (this.getProcedureSourceValue() == null ^ other.getProcedureSourceValue()==null) return false;
		if (this.getProcedureSourceValue() != null && other.getProcedureSourceValue()!=null) {
			if (!this.getProcedureSourceValue().equals(other.getProcedureSourceValue())) return false;
		}
		//testing this.getProcedureSourceConceptId()
		if (this.getProcedureSourceConceptId() == null ^ other.getProcedureSourceConceptId()==null) return false;
		if (this.getProcedureSourceConceptId() != null && other.getProcedureSourceConceptId()!=null) {
			if (!this.getProcedureSourceConceptId().equals(other.getProcedureSourceConceptId())) return false;
		}
		//testing this.getModifierSourceValue()
		if (this.getModifierSourceValue() == null ^ other.getModifierSourceValue()==null) return false;
		if (this.getModifierSourceValue() != null && other.getModifierSourceValue()!=null) {
			if (!this.getModifierSourceValue().equals(other.getModifierSourceValue())) return false;
		}
		return true;
	}
	
}
