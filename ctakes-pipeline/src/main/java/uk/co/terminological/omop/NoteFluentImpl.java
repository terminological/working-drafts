package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.NoteFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class NoteFluentImpl extends Observable implements Note, NoteFluent  {

	public static NoteFluent create() {
		return new NoteFluentImpl();
	}

	// Fields
	// ======

	private Long _noteId;
	private Long _personId;
	private Long _noteEventId;
	private Integer _noteEventFieldConceptId;
	private Date _noteDate;
	private Timestamp _noteDatetime;
	private Integer _noteTypeConceptId;
	private Integer _noteClassConceptId;
	private String _noteTitle;
	private String _noteText;
	private Integer _encodingConceptId;
	private Integer _languageConceptId;
	private Long _providerId;
	private Long _visitOccurrenceId;
	private Long _visitDetailId;
	private String _noteSourceValue;

	// Public constructor
	// ==================

	public NoteFluentImpl() {}

	public NoteFluentImpl(
		Long _noteId,
		Long _personId,
		Long _noteEventId,
		Integer _noteEventFieldConceptId,
		Date _noteDate,
		Timestamp _noteDatetime,
		Integer _noteTypeConceptId,
		Integer _noteClassConceptId,
		String _noteTitle,
		String _noteText,
		Integer _encodingConceptId,
		Integer _languageConceptId,
		Long _providerId,
		Long _visitOccurrenceId,
		Long _visitDetailId,
		String _noteSourceValue
	) {
		this._noteId = _noteId;
		this._personId = _personId;
		this._noteEventId = _noteEventId;
		this._noteEventFieldConceptId = _noteEventFieldConceptId;
		this._noteDate = _noteDate;
		this._noteDatetime = _noteDatetime;
		this._noteTypeConceptId = _noteTypeConceptId;
		this._noteClassConceptId = _noteClassConceptId;
		this._noteTitle = _noteTitle;
		this._noteText = _noteText;
		this._encodingConceptId = _encodingConceptId;
		this._languageConceptId = _languageConceptId;
		this._providerId = _providerId;
		this._visitOccurrenceId = _visitOccurrenceId;
		this._visitDetailId = _visitDetailId;
		this._noteSourceValue = _noteSourceValue;
	}
	
	public NoteFluentImpl(Note clone) {
		this._noteId = clone.getNoteId();
		this._personId = clone.getPersonId();
		this._noteEventId = clone.getNoteEventId();
		this._noteEventFieldConceptId = clone.getNoteEventFieldConceptId();
		this._noteDate = (Date) clone.getNoteDate().clone();
		this._noteDatetime = (Timestamp) clone.getNoteDatetime().clone();
		this._noteTypeConceptId = clone.getNoteTypeConceptId();
		this._noteClassConceptId = clone.getNoteClassConceptId();
		this._noteTitle = clone.getNoteTitle();
		this._noteText = clone.getNoteText();
		this._encodingConceptId = clone.getEncodingConceptId();
		this._languageConceptId = clone.getLanguageConceptId();
		this._providerId = clone.getProviderId();
		this._visitOccurrenceId = clone.getVisitOccurrenceId();
		this._visitDetailId = clone.getVisitDetailId();
		this._noteSourceValue = clone.getNoteSourceValue();
	}
	
	public NoteFluentImpl clone() {
		return new NoteFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getNoteId() {
		return _noteId;
	}
	public Long getPersonId() {
		return _personId;
	}
	public Long getNoteEventId() {
		return _noteEventId;
	}
	public Integer getNoteEventFieldConceptId() {
		return _noteEventFieldConceptId;
	}
	public Date getNoteDate() {
		return _noteDate;
	}
	public Timestamp getNoteDatetime() {
		return _noteDatetime;
	}
	public Integer getNoteTypeConceptId() {
		return _noteTypeConceptId;
	}
	public Integer getNoteClassConceptId() {
		return _noteClassConceptId;
	}
	public String getNoteTitle() {
		return _noteTitle;
	}
	public String getNoteText() {
		return _noteText;
	}
	public Integer getEncodingConceptId() {
		return _encodingConceptId;
	}
	public Integer getLanguageConceptId() {
		return _languageConceptId;
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
	public String getNoteSourceValue() {
		return _noteSourceValue;
	}
	
	// POJO Setters
	// ============
	
	public void setNoteId(Long value) {
		this._noteId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setPersonId(Long value) {
		this._personId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteEventId(Long value) {
		this._noteEventId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteEventFieldConceptId(Integer value) {
		this._noteEventFieldConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteDate(Date value) {
		this._noteDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteDatetime(Timestamp value) {
		this._noteDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteTypeConceptId(Integer value) {
		this._noteTypeConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteClassConceptId(Integer value) {
		this._noteClassConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteTitle(String value) {
		this._noteTitle = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteText(String value) {
		this._noteText = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setEncodingConceptId(Integer value) {
		this._encodingConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setLanguageConceptId(Integer value) {
		this._languageConceptId = value;
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
	
	public void setNoteSourceValue(String value) {
		this._noteSourceValue = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public NoteFluent withNoteId(Long value) {
		setNoteId(value);
		return this;
	}
	public NoteFluent withPersonId(Long value) {
		setPersonId(value);
		return this;
	}
	public NoteFluent withNoteEventId(Long value) {
		setNoteEventId(value);
		return this;
	}
	public NoteFluent withNoteEventFieldConceptId(Integer value) {
		setNoteEventFieldConceptId(value);
		return this;
	}
	public NoteFluent withNoteDate(Date value) {
		setNoteDate(value);
		return this;
	}
	public NoteFluent withNoteDatetime(Timestamp value) {
		setNoteDatetime(value);
		return this;
	}
	public NoteFluent withNoteTypeConceptId(Integer value) {
		setNoteTypeConceptId(value);
		return this;
	}
	public NoteFluent withNoteClassConceptId(Integer value) {
		setNoteClassConceptId(value);
		return this;
	}
	public NoteFluent withNoteTitle(String value) {
		setNoteTitle(value);
		return this;
	}
	public NoteFluent withNoteText(String value) {
		setNoteText(value);
		return this;
	}
	public NoteFluent withEncodingConceptId(Integer value) {
		setEncodingConceptId(value);
		return this;
	}
	public NoteFluent withLanguageConceptId(Integer value) {
		setLanguageConceptId(value);
		return this;
	}
	public NoteFluent withProviderId(Long value) {
		setProviderId(value);
		return this;
	}
	public NoteFluent withVisitOccurrenceId(Long value) {
		setVisitOccurrenceId(value);
		return this;
	}
	public NoteFluent withVisitDetailId(Long value) {
		setVisitDetailId(value);
		return this;
	}
	public NoteFluent withNoteSourceValue(String value) {
		setNoteSourceValue(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
		result = prime * result + ((getPersonId() == null) ? 0 : getPersonId().hashCode());
		result = prime * result + ((getNoteEventId() == null) ? 0 : getNoteEventId().hashCode());
		result = prime * result + ((getNoteEventFieldConceptId() == null) ? 0 : getNoteEventFieldConceptId().hashCode());
		result = prime * result + ((getNoteDate() == null) ? 0 : getNoteDate().hashCode());
		result = prime * result + ((getNoteDatetime() == null) ? 0 : getNoteDatetime().hashCode());
		result = prime * result + ((getNoteTypeConceptId() == null) ? 0 : getNoteTypeConceptId().hashCode());
		result = prime * result + ((getNoteClassConceptId() == null) ? 0 : getNoteClassConceptId().hashCode());
		result = prime * result + ((getNoteTitle() == null) ? 0 : getNoteTitle().hashCode());
		result = prime * result + ((getNoteText() == null) ? 0 : getNoteText().hashCode());
		result = prime * result + ((getEncodingConceptId() == null) ? 0 : getEncodingConceptId().hashCode());
		result = prime * result + ((getLanguageConceptId() == null) ? 0 : getLanguageConceptId().hashCode());
		result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
		result = prime * result + ((getVisitOccurrenceId() == null) ? 0 : getVisitOccurrenceId().hashCode());
		result = prime * result + ((getVisitDetailId() == null) ? 0 : getVisitDetailId().hashCode());
		result = prime * result + ((getNoteSourceValue() == null) ? 0 : getNoteSourceValue().hashCode());
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
		NoteFluentImpl other = (NoteFluentImpl) obj;
		//testing this.getNoteId()
		if (this.getNoteId() == null ^ other.getNoteId()==null) return false;
		if (this.getNoteId() != null && other.getNoteId()!=null) {
			if (!this.getNoteId().equals(other.getNoteId())) return false;
		}
		//testing this.getPersonId()
		if (this.getPersonId() == null ^ other.getPersonId()==null) return false;
		if (this.getPersonId() != null && other.getPersonId()!=null) {
			if (!this.getPersonId().equals(other.getPersonId())) return false;
		}
		//testing this.getNoteEventId()
		if (this.getNoteEventId() == null ^ other.getNoteEventId()==null) return false;
		if (this.getNoteEventId() != null && other.getNoteEventId()!=null) {
			if (!this.getNoteEventId().equals(other.getNoteEventId())) return false;
		}
		//testing this.getNoteEventFieldConceptId()
		if (this.getNoteEventFieldConceptId() == null ^ other.getNoteEventFieldConceptId()==null) return false;
		if (this.getNoteEventFieldConceptId() != null && other.getNoteEventFieldConceptId()!=null) {
			if (!this.getNoteEventFieldConceptId().equals(other.getNoteEventFieldConceptId())) return false;
		}
		//testing this.getNoteDate()
		if (this.getNoteDate() == null ^ other.getNoteDate()==null) return false;
		if (this.getNoteDate() != null && other.getNoteDate()!=null) {
			if (!this.getNoteDate().equals(other.getNoteDate())) return false;
		}
		//testing this.getNoteDatetime()
		if (this.getNoteDatetime() == null ^ other.getNoteDatetime()==null) return false;
		if (this.getNoteDatetime() != null && other.getNoteDatetime()!=null) {
			if (!this.getNoteDatetime().equals(other.getNoteDatetime())) return false;
		}
		//testing this.getNoteTypeConceptId()
		if (this.getNoteTypeConceptId() == null ^ other.getNoteTypeConceptId()==null) return false;
		if (this.getNoteTypeConceptId() != null && other.getNoteTypeConceptId()!=null) {
			if (!this.getNoteTypeConceptId().equals(other.getNoteTypeConceptId())) return false;
		}
		//testing this.getNoteClassConceptId()
		if (this.getNoteClassConceptId() == null ^ other.getNoteClassConceptId()==null) return false;
		if (this.getNoteClassConceptId() != null && other.getNoteClassConceptId()!=null) {
			if (!this.getNoteClassConceptId().equals(other.getNoteClassConceptId())) return false;
		}
		//testing this.getNoteTitle()
		if (this.getNoteTitle() == null ^ other.getNoteTitle()==null) return false;
		if (this.getNoteTitle() != null && other.getNoteTitle()!=null) {
			if (!this.getNoteTitle().equals(other.getNoteTitle())) return false;
		}
		//testing this.getNoteText()
		if (this.getNoteText() == null ^ other.getNoteText()==null) return false;
		if (this.getNoteText() != null && other.getNoteText()!=null) {
			if (!this.getNoteText().equals(other.getNoteText())) return false;
		}
		//testing this.getEncodingConceptId()
		if (this.getEncodingConceptId() == null ^ other.getEncodingConceptId()==null) return false;
		if (this.getEncodingConceptId() != null && other.getEncodingConceptId()!=null) {
			if (!this.getEncodingConceptId().equals(other.getEncodingConceptId())) return false;
		}
		//testing this.getLanguageConceptId()
		if (this.getLanguageConceptId() == null ^ other.getLanguageConceptId()==null) return false;
		if (this.getLanguageConceptId() != null && other.getLanguageConceptId()!=null) {
			if (!this.getLanguageConceptId().equals(other.getLanguageConceptId())) return false;
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
		//testing this.getNoteSourceValue()
		if (this.getNoteSourceValue() == null ^ other.getNoteSourceValue()==null) return false;
		if (this.getNoteSourceValue() != null && other.getNoteSourceValue()!=null) {
			if (!this.getNoteSourceValue().equals(other.getNoteSourceValue())) return false;
		}
		return true;
	}
	
}
