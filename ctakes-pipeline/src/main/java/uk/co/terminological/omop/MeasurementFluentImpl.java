package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.MeasurementFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class MeasurementFluentImpl extends Observable implements Measurement, MeasurementFluent  {

	public static MeasurementFluent create() {
		return new MeasurementFluentImpl();
	}

	// Fields
	// ======

	private Long _measurementId;
	private Long _personId;
	private Integer _measurementConceptId;
	private Date _measurementDate;
	private Timestamp _measurementDatetime;
	private String _measurementTime;
	private Integer _measurementTypeConceptId;
	private Integer _operatorConceptId;
	private Double _valueAsNumber;
	private Integer _valueAsConceptId;
	private Integer _unitConceptId;
	private Double _rangeLow;
	private Double _rangeHigh;
	private Long _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _measurementSourceValue;
	private Integer _measurementSourceConceptId;
	private String _unitSourceValue;
	private String _valueSourceValue;

	// Public constructor
	// ==================

	public MeasurementFluentImpl() {}

	public MeasurementFluentImpl(
		Long _measurementId,
		Long _personId,
		Integer _measurementConceptId,
		Date _measurementDate,
		Timestamp _measurementDatetime,
		String _measurementTime,
		Integer _measurementTypeConceptId,
		Integer _operatorConceptId,
		Double _valueAsNumber,
		Integer _valueAsConceptId,
		Integer _unitConceptId,
		Double _rangeLow,
		Double _rangeHigh,
		Long _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _measurementSourceValue,
		Integer _measurementSourceConceptId,
		String _unitSourceValue,
		String _valueSourceValue
	) {
		this._measurementId = _measurementId;
		this._personId = _personId;
		this._measurementConceptId = _measurementConceptId;
		this._measurementDate = _measurementDate;
		this._measurementDatetime = _measurementDatetime;
		this._measurementTime = _measurementTime;
		this._measurementTypeConceptId = _measurementTypeConceptId;
		this._operatorConceptId = _operatorConceptId;
		this._valueAsNumber = _valueAsNumber;
		this._valueAsConceptId = _valueAsConceptId;
		this._unitConceptId = _unitConceptId;
		this._rangeLow = _rangeLow;
		this._rangeHigh = _rangeHigh;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._measurementSourceValue = _measurementSourceValue;
		this._measurementSourceConceptId = _measurementSourceConceptId;
		this._unitSourceValue = _unitSourceValue;
		this._valueSourceValue = _valueSourceValue;
	}
	
	@SuppressWarnings("unchecked")
	public MeasurementFluentImpl(Measurement clone) {
		this._measurementId = clone.getMeasurementId();
		this._personId = clone.getPersonId();
		this._measurementConceptId = clone.getMeasurementConceptId();
		this._measurementDate = (Date) clone.getMeasurementDate().clone();
		this._measurementDatetime = (Timestamp) clone.getMeasurementDatetime().clone();
		this._measurementTime = clone.getMeasurementTime();
		this._measurementTypeConceptId = clone.getMeasurementTypeConceptId();
		this._operatorConceptId = clone.getOperatorConceptId();
		this._valueAsNumber = clone.getValueAsNumber();
		this._valueAsConceptId = clone.getValueAsConceptId();
		this._unitConceptId = clone.getUnitConceptId();
		this._rangeLow = clone.getRangeLow();
		this._rangeHigh = clone.getRangeHigh();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._measurementSourceValue = clone.getMeasurementSourceValue();
		this._measurementSourceConceptId = clone.getMeasurementSourceConceptId();
		this._unitSourceValue = clone.getUnitSourceValue();
		this._valueSourceValue = clone.getValueSourceValue();
	}
	
	public MeasurementFluentImpl clone() {
		return new MeasurementFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getMeasurementId() {
		return _measurementId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Integer getMeasurementConceptId() {
		return _measurementConceptId;
	}
	public Date getMeasurementDate() {
		return _measurementDate;
	}
	public Timestamp getMeasurementDatetime() {
		return _measurementDatetime;
	}
	public String getMeasurementTime() {
		return _measurementTime;
	}
	public Integer getMeasurementTypeConceptId() {
		return _measurementTypeConceptId;
	}
	public Integer getOperatorConceptId() {
		return _operatorConceptId;
	}
	public Double getValueAsNumber() {
		return _valueAsNumber;
	}
	public Integer getValueAsConceptId() {
		return _valueAsConceptId;
	}
	public Integer getUnitConceptId() {
		return _unitConceptId;
	}
	public Double getRangeLow() {
		return _rangeLow;
	}
	public Double getRangeHigh() {
		return _rangeHigh;
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
	public String getMeasurementSourceValue() {
		return _measurementSourceValue;
	}
	public Integer getMeasurementSourceConceptId() {
		return _measurementSourceConceptId;
	}
	public String getUnitSourceValue() {
		return _unitSourceValue;
	}
	public String getValueSourceValue() {
		return _valueSourceValue;
	}
	
	// POJO Setters
	// ============
	
	public void setMeasurementId(Long value) {
		this._measurementId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementConceptId(Integer value) {
		this._measurementConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementDate(Date value) {
		this._measurementDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementDatetime(Timestamp value) {
		this._measurementDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementTime(String value) {
		this._measurementTime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementTypeConceptId(Integer value) {
		this._measurementTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setOperatorConceptId(Integer value) {
		this._operatorConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsNumber(Double value) {
		this._valueAsNumber = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueAsConceptId(Integer value) {
		this._valueAsConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setUnitConceptId(Integer value) {
		this._unitConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setRangeLow(Double value) {
		this._rangeLow = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setRangeHigh(Double value) {
		this._rangeHigh = value;
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
	
	public void setMeasurementSourceValue(String value) {
		this._measurementSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setMeasurementSourceConceptId(Integer value) {
		this._measurementSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setUnitSourceValue(String value) {
		this._unitSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setValueSourceValue(String value) {
		this._valueSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public MeasurementFluent withMeasurementId(Long value) {
		setMeasurementId(value);
		return this;
	}
	public MeasurementFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public MeasurementFluent withMeasurementConceptId(Integer value) {
		setMeasurementConceptId(value);
		return this;
	}
	public MeasurementFluent withMeasurementDate(Date value) {
		setMeasurementDate(value);
		return this;
	}
	public MeasurementFluent withMeasurementDatetime(Timestamp value) {
		setMeasurementDatetime(value);
		return this;
	}
	public MeasurementFluent withMeasurementTime(String value) {
		setMeasurementTime(value);
		return this;
	}
	public MeasurementFluent withMeasurementTypeConceptId(Integer value) {
		setMeasurementTypeConceptId(value);
		return this;
	}
	public MeasurementFluent withOperatorConceptId(Integer value) {
		setOperatorConceptId(value);
		return this;
	}
	public MeasurementFluent withValueAsNumber(Double value) {
		setValueAsNumber(value);
		return this;
	}
	public MeasurementFluent withValueAsConceptId(Integer value) {
		setValueAsConceptId(value);
		return this;
	}
	public MeasurementFluent withUnitConceptId(Integer value) {
		setUnitConceptId(value);
		return this;
	}
	public MeasurementFluent withRangeLow(Double value) {
		setRangeLow(value);
		return this;
	}
	public MeasurementFluent withRangeHigh(Double value) {
		setRangeHigh(value);
		return this;
	}
	public MeasurementFluent withProviderId(Long value) {
		setProviderId(value);
		return this;
	}
	public MeasurementFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public MeasurementFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public MeasurementFluent withMeasurementSourceValue(String value) {
		setMeasurementSourceValue(value);
		return this;
	}
	public MeasurementFluent withMeasurementSourceConceptId(Integer value) {
		setMeasurementSourceConceptId(value);
		return this;
	}
	public MeasurementFluent withUnitSourceValue(String value) {
		setUnitSourceValue(value);
		return this;
	}
	public MeasurementFluent withValueSourceValue(String value) {
		setValueSourceValue(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMeasurementId() == null) ? 0 : getMeasurementId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getMeasurementConceptId() == null) ? 0 : getMeasurementConceptId().hashCode());
		result = prime * result + ((getMeasurementDate() == null) ? 0 : getMeasurementDate().hashCode());
		result = prime * result + ((getMeasurementDatetime() == null) ? 0 : getMeasurementDatetime().hashCode());
		result = prime * result + ((getMeasurementTime() == null) ? 0 : getMeasurementTime().hashCode());
		result = prime * result + ((getMeasurementTypeConceptId() == null) ? 0 : getMeasurementTypeConceptId().hashCode());
		result = prime * result + ((getOperatorConceptId() == null) ? 0 : getOperatorConceptId().hashCode());
		result = prime * result + ((getValueAsNumber() == null) ? 0 : getValueAsNumber().hashCode());
		result = prime * result + ((getValueAsConceptId() == null) ? 0 : getValueAsConceptId().hashCode());
		result = prime * result + ((getUnitConceptId() == null) ? 0 : getUnitConceptId().hashCode());
		result = prime * result + ((getRangeLow() == null) ? 0 : getRangeLow().hashCode());
		result = prime * result + ((getRangeHigh() == null) ? 0 : getRangeHigh().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getMeasurementSourceValue() == null) ? 0 : getMeasurementSourceValue().hashCode());
		result = prime * result + ((getMeasurementSourceConceptId() == null) ? 0 : getMeasurementSourceConceptId().hashCode());
		result = prime * result + ((getUnitSourceValue() == null) ? 0 : getUnitSourceValue().hashCode());
		result = prime * result + ((getValueSourceValue() == null) ? 0 : getValueSourceValue().hashCode());
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
		MeasurementFluentImpl other = (MeasurementFluentImpl) obj;
		//testing this.getMeasurementId()
		if (this.getMeasurementId() == null ^ other.getMeasurementId()==null) return false;
		if (this.getMeasurementId() != null && other.getMeasurementId()!=null) {
			if (!this.getMeasurementId().equals(other.getMeasurementId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getMeasurementConceptId()
		if (this.getMeasurementConceptId() == null ^ other.getMeasurementConceptId()==null) return false;
		if (this.getMeasurementConceptId() != null && other.getMeasurementConceptId()!=null) {
			if (!this.getMeasurementConceptId().equals(other.getMeasurementConceptId())) return false;
		}
		//testing this.getMeasurementDate()
		if (this.getMeasurementDate() == null ^ other.getMeasurementDate()==null) return false;
		if (this.getMeasurementDate() != null && other.getMeasurementDate()!=null) {
			if (!this.getMeasurementDate().equals(other.getMeasurementDate())) return false;
		}
		//testing this.getMeasurementDatetime()
		if (this.getMeasurementDatetime() == null ^ other.getMeasurementDatetime()==null) return false;
		if (this.getMeasurementDatetime() != null && other.getMeasurementDatetime()!=null) {
			if (!this.getMeasurementDatetime().equals(other.getMeasurementDatetime())) return false;
		}
		//testing this.getMeasurementTime()
		if (this.getMeasurementTime() == null ^ other.getMeasurementTime()==null) return false;
		if (this.getMeasurementTime() != null && other.getMeasurementTime()!=null) {
			if (!this.getMeasurementTime().equals(other.getMeasurementTime())) return false;
		}
		//testing this.getMeasurementTypeConceptId()
		if (this.getMeasurementTypeConceptId() == null ^ other.getMeasurementTypeConceptId()==null) return false;
		if (this.getMeasurementTypeConceptId() != null && other.getMeasurementTypeConceptId()!=null) {
			if (!this.getMeasurementTypeConceptId().equals(other.getMeasurementTypeConceptId())) return false;
		}
		//testing this.getOperatorConceptId()
		if (this.getOperatorConceptId() == null ^ other.getOperatorConceptId()==null) return false;
		if (this.getOperatorConceptId() != null && other.getOperatorConceptId()!=null) {
			if (!this.getOperatorConceptId().equals(other.getOperatorConceptId())) return false;
		}
		//testing this.getValueAsNumber()
		if (this.getValueAsNumber() == null ^ other.getValueAsNumber()==null) return false;
		if (this.getValueAsNumber() != null && other.getValueAsNumber()!=null) {
			if (!this.getValueAsNumber().equals(other.getValueAsNumber())) return false;
		}
		//testing this.getValueAsConceptId()
		if (this.getValueAsConceptId() == null ^ other.getValueAsConceptId()==null) return false;
		if (this.getValueAsConceptId() != null && other.getValueAsConceptId()!=null) {
			if (!this.getValueAsConceptId().equals(other.getValueAsConceptId())) return false;
		}
		//testing this.getUnitConceptId()
		if (this.getUnitConceptId() == null ^ other.getUnitConceptId()==null) return false;
		if (this.getUnitConceptId() != null && other.getUnitConceptId()!=null) {
			if (!this.getUnitConceptId().equals(other.getUnitConceptId())) return false;
		}
		//testing this.getRangeLow()
		if (this.getRangeLow() == null ^ other.getRangeLow()==null) return false;
		if (this.getRangeLow() != null && other.getRangeLow()!=null) {
			if (!this.getRangeLow().equals(other.getRangeLow())) return false;
		}
		//testing this.getRangeHigh()
		if (this.getRangeHigh() == null ^ other.getRangeHigh()==null) return false;
		if (this.getRangeHigh() != null && other.getRangeHigh()!=null) {
			if (!this.getRangeHigh().equals(other.getRangeHigh())) return false;
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
		//testing this.getMeasurementSourceValue()
		if (this.getMeasurementSourceValue() == null ^ other.getMeasurementSourceValue()==null) return false;
		if (this.getMeasurementSourceValue() != null && other.getMeasurementSourceValue()!=null) {
			if (!this.getMeasurementSourceValue().equals(other.getMeasurementSourceValue())) return false;
		}
		//testing this.getMeasurementSourceConceptId()
		if (this.getMeasurementSourceConceptId() == null ^ other.getMeasurementSourceConceptId()==null) return false;
		if (this.getMeasurementSourceConceptId() != null && other.getMeasurementSourceConceptId()!=null) {
			if (!this.getMeasurementSourceConceptId().equals(other.getMeasurementSourceConceptId())) return false;
		}
		//testing this.getUnitSourceValue()
		if (this.getUnitSourceValue() == null ^ other.getUnitSourceValue()==null) return false;
		if (this.getUnitSourceValue() != null && other.getUnitSourceValue()!=null) {
			if (!this.getUnitSourceValue().equals(other.getUnitSourceValue())) return false;
		}
		//testing this.getValueSourceValue()
		if (this.getValueSourceValue() == null ^ other.getValueSourceValue()==null) return false;
		if (this.getValueSourceValue() != null && other.getValueSourceValue()!=null) {
			if (!this.getValueSourceValue().equals(other.getValueSourceValue())) return false;
		}
		return true;
	}
	
}
