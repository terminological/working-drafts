package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.util.*;
import uk.co.terminological.omop.CuiOmopMapFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class CuiOmopMapFluentImpl extends Observable implements CuiOmopMap, CuiOmopMapFluent  {

	public static CuiOmopMapFluent create() {
		return new CuiOmopMapFluentImpl();
	}

	// Fields
	// ======

	private String _cui;
	private String _code;
	private Integer _conceptId;

	// Public constructor
	// ==================

	public CuiOmopMapFluentImpl() {}

	public CuiOmopMapFluentImpl(
		String _cui,
		String _code,
		Integer _conceptId
	) {
		this._cui = _cui;
		this._code = _code;
		this._conceptId = _conceptId;
	}
	
	public CuiOmopMapFluentImpl(CuiOmopMap clone) {
		this._cui = clone.getCui();
		this._code = clone.getCode();
		this._conceptId = clone.getConceptId();
	}
	
	public CuiOmopMapFluentImpl clone() {
		return new CuiOmopMapFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public String getCui() {
		return _cui;
	}
	public String getCode() {
		return _code;
	}
	public Integer getConceptId() {
		return _conceptId;
	}
	
	// POJO Setters
	// ============
	
	public void setCui(String value) {
		this._cui = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setCode(String value) {
		this._code = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setConceptId(Integer value) {
		this._conceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public CuiOmopMapFluent withCui(String value) {
		setCui(value);
		return this;
	}
	public CuiOmopMapFluent withCode(String value) {
		setCode(value);
		return this;
	}
	public CuiOmopMapFluent withConceptId(Integer value) {
		setConceptId(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCui() == null) ? 0 : getCui().hashCode());
		result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
		result = prime * result + ((getConceptId() == null) ? 0 : getConceptId().hashCode());
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
		CuiOmopMapFluentImpl other = (CuiOmopMapFluentImpl) obj;
		//testing this.getCui()
		if (this.getCui() == null ^ other.getCui()==null) return false;
		if (this.getCui() != null && other.getCui()!=null) {
			if (!this.getCui().equals(other.getCui())) return false;
		}
		//testing this.getCode()
		if (this.getCode() == null ^ other.getCode()==null) return false;
		if (this.getCode() != null && other.getCode()!=null) {
			if (!this.getCode().equals(other.getCode())) return false;
		}
		//testing this.getConceptId()
		if (this.getConceptId() == null ^ other.getConceptId()==null) return false;
		if (this.getConceptId() != null && other.getConceptId()!=null) {
			if (!this.getConceptId().equals(other.getConceptId())) return false;
		}
		return true;
	}
	
}
