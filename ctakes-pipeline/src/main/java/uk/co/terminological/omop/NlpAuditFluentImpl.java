package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.NlpAuditFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class NlpAuditFluentImpl extends Observable implements NlpAudit, NlpAuditFluent  {

	public static NlpAuditFluent create() {
		return new NlpAuditFluentImpl();
	}

	// Fields
	// ======

	private Long _noteId;
	private Timestamp _eventTime;
	private String _nlpSystem;
	private String _nlpSystemInstance;
	private String _eventType;
	private String _eventDetail;
	private Integer _priority;

	// Public constructor
	// ==================

	public NlpAuditFluentImpl() {}

	public NlpAuditFluentImpl(
		Long _noteId,
		Timestamp _eventTime,
		String _nlpSystem,
		String _nlpSystemInstance,
		String _eventType,
		String _eventDetail,
		Integer _priority
	) {
		this._noteId = _noteId;
		this._eventTime = _eventTime;
		this._nlpSystem = _nlpSystem;
		this._nlpSystemInstance = _nlpSystemInstance;
		this._eventType = _eventType;
		this._eventDetail = _eventDetail;
		this._priority = _priority;
	}
	
	public NlpAuditFluentImpl(NlpAudit clone) {
		this._noteId = clone.getNoteId();
		this._eventTime = (Timestamp) clone.getEventTime().clone();
		this._nlpSystem = clone.getNlpSystem();
		this._nlpSystemInstance = clone.getNlpSystemInstance();
		this._eventType = clone.getEventType();
		this._eventDetail = clone.getEventDetail();
		this._priority = clone.getPriority();
	}
	
	public NlpAuditFluentImpl clone() {
		return new NlpAuditFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getNoteId() {
		return _noteId;
	}
	public Timestamp getEventTime() {
		return _eventTime;
	}
	public String getNlpSystem() {
		return _nlpSystem;
	}
	public String getNlpSystemInstance() {
		return _nlpSystemInstance;
	}
	public String getEventType() {
		return _eventType;
	}
	public String getEventDetail() {
		return _eventDetail;
	}
	public Integer getPriority() {
		return _priority;
	}
	
	// POJO Setters
	// ============
	
	public void setNoteId(Long value) {
		this._noteId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setEventTime(Timestamp value) {
		this._eventTime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNlpSystem(String value) {
		this._nlpSystem = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNlpSystemInstance(String value) {
		this._nlpSystemInstance = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setEventType(String value) {
		this._eventType = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setEventDetail(String value) {
		this._eventDetail = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPriority(Integer value) {
		this._priority = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public NlpAuditFluent withNoteId(Long value) {
		setNoteId(value);
		return this;
	}
	public NlpAuditFluent withEventTime(Timestamp value) {
		setEventTime(value);
		return this;
	}
	public NlpAuditFluent withNlpSystem(String value) {
		setNlpSystem(value);
		return this;
	}
	public NlpAuditFluent withNlpSystemInstance(String value) {
		setNlpSystemInstance(value);
		return this;
	}
	public NlpAuditFluent withEventType(String value) {
		setEventType(value);
		return this;
	}
	public NlpAuditFluent withEventDetail(String value) {
		setEventDetail(value);
		return this;
	}
	public NlpAuditFluent withPriority(Integer value) {
		setPriority(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
		result = prime * result + ((getEventTime() == null) ? 0 : getEventTime().hashCode());
		result = prime * result + ((getNlpSystem() == null) ? 0 : getNlpSystem().hashCode());
		result = prime * result + ((getNlpSystemInstance() == null) ? 0 : getNlpSystemInstance().hashCode());
		result = prime * result + ((getEventType() == null) ? 0 : getEventType().hashCode());
		result = prime * result + ((getEventDetail() == null) ? 0 : getEventDetail().hashCode());
		result = prime * result + ((getPriority() == null) ? 0 : getPriority().hashCode());
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
		NlpAuditFluentImpl other = (NlpAuditFluentImpl) obj;
		//testing this.getNoteId()
		if (this.getNoteId() == null ^ other.getNoteId()==null) return false;
		if (this.getNoteId() != null && other.getNoteId()!=null) {
			if (!this.getNoteId().equals(other.getNoteId())) return false;
		}
		//testing this.getEventTime()
		if (this.getEventTime() == null ^ other.getEventTime()==null) return false;
		if (this.getEventTime() != null && other.getEventTime()!=null) {
			if (!this.getEventTime().equals(other.getEventTime())) return false;
		}
		//testing this.getNlpSystem()
		if (this.getNlpSystem() == null ^ other.getNlpSystem()==null) return false;
		if (this.getNlpSystem() != null && other.getNlpSystem()!=null) {
			if (!this.getNlpSystem().equals(other.getNlpSystem())) return false;
		}
		//testing this.getNlpSystemInstance()
		if (this.getNlpSystemInstance() == null ^ other.getNlpSystemInstance()==null) return false;
		if (this.getNlpSystemInstance() != null && other.getNlpSystemInstance()!=null) {
			if (!this.getNlpSystemInstance().equals(other.getNlpSystemInstance())) return false;
		}
		//testing this.getEventType()
		if (this.getEventType() == null ^ other.getEventType()==null) return false;
		if (this.getEventType() != null && other.getEventType()!=null) {
			if (!this.getEventType().equals(other.getEventType())) return false;
		}
		//testing this.getEventDetail()
		if (this.getEventDetail() == null ^ other.getEventDetail()==null) return false;
		if (this.getEventDetail() != null && other.getEventDetail()!=null) {
			if (!this.getEventDetail().equals(other.getEventDetail())) return false;
		}
		//testing this.getPriority()
		if (this.getPriority() == null ^ other.getPriority()==null) return false;
		if (this.getPriority() != null && other.getPriority()!=null) {
			if (!this.getPriority().equals(other.getPriority())) return false;
		}
		return true;
	}
	
}
