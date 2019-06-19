package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.NoteNlpFluent;


@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class NoteNlpFluentImpl extends Observable implements NoteNlp, NoteNlpFluent  {

	public static NoteNlpFluent create() {
		return new NoteNlpFluentImpl();
	}

	// Fields
	// ======

	private Long _noteNlpId;
	private Long _noteId;
	private Integer _sectionConceptId;
	private String _snippet;
	private String _offset;
	private String _lexicalVariant;
	private Integer _noteNlpConceptId;
	private String _nlpSystem;
	private Date _nlpDate;
	private Timestamp _nlpDatetime;
	private String _termExists;
	private String _termTemporal;
	private String _termModifiers;
	private Integer _noteNlpSourceConceptId;
	private Integer _customCode;

	// Public constructor
	// ==================

	public NoteNlpFluentImpl() {}

	public NoteNlpFluentImpl(
		Long _noteNlpId,
		Long _noteId,
		Integer _sectionConceptId,
		String _snippet,
		String _offset,
		String _lexicalVariant,
		Integer _noteNlpConceptId,
		String _nlpSystem,
		Date _nlpDate,
		Timestamp _nlpDatetime,
		String _termExists,
		String _termTemporal,
		String _termModifiers,
		Integer _noteNlpSourceConceptId,
		Integer _customCode
	) {
		this._noteNlpId = _noteNlpId;
		this._noteId = _noteId;
		this._sectionConceptId = _sectionConceptId;
		this._snippet = _snippet;
		this._offset = _offset;
		this._lexicalVariant = _lexicalVariant;
		this._noteNlpConceptId = _noteNlpConceptId;
		this._nlpSystem = _nlpSystem;
		this._nlpDate = _nlpDate;
		this._nlpDatetime = _nlpDatetime;
		this._termExists = _termExists;
		this._termTemporal = _termTemporal;
		this._termModifiers = _termModifiers;
		this._noteNlpSourceConceptId = _noteNlpSourceConceptId;
		this._customCode = _customCode;
	}
	
	public NoteNlpFluentImpl(NoteNlp clone) {
		this._noteNlpId = clone.getNoteNlpId();
		this._noteId = clone.getNoteId();
		this._sectionConceptId = clone.getSectionConceptId();
		this._snippet = clone.getSnippet();
		this._offset = clone.getOffset();
		this._lexicalVariant = clone.getLexicalVariant();
		this._noteNlpConceptId = clone.getNoteNlpConceptId();
		this._nlpSystem = clone.getNlpSystem();
		this._nlpDate = (Date) clone.getNlpDate().clone();
		this._nlpDatetime = (Timestamp) clone.getNlpDatetime().clone();
		this._termExists = clone.getTermExists();
		this._termTemporal = clone.getTermTemporal();
		this._termModifiers = clone.getTermModifiers();
		this._noteNlpSourceConceptId = clone.getNoteNlpSourceConceptId();
		this._customCode = clone.getCustomCode();
	}
	
	public NoteNlpFluentImpl clone() {
		return new NoteNlpFluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	public Long getNoteNlpId() {
		return _noteNlpId;
	}
	public Long getNoteId() {
		return _noteId;
	}
	public Integer getSectionConceptId() {
		return _sectionConceptId;
	}
	public String getSnippet() {
		return _snippet;
	}
	public String getOffset() {
		return _offset;
	}
	public String getLexicalVariant() {
		return _lexicalVariant;
	}
	public Integer getNoteNlpConceptId() {
		return _noteNlpConceptId;
	}
	public String getNlpSystem() {
		return _nlpSystem;
	}
	public Date getNlpDate() {
		return _nlpDate;
	}
	public Timestamp getNlpDatetime() {
		return _nlpDatetime;
	}
	public String getTermExists() {
		return _termExists;
	}
	public String getTermTemporal() {
		return _termTemporal;
	}
	public String getTermModifiers() {
		return _termModifiers;
	}
	public Integer getNoteNlpSourceConceptId() {
		return _noteNlpSourceConceptId;
	}
	public Integer getCustomCode() {
		return _customCode;
	}
	
	// POJO Setters
	// ============
	
	public void setNoteNlpId(Long value) {
		this._noteNlpId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteId(Long value) {
		this._noteId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setSectionConceptId(Integer value) {
		this._sectionConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setSnippet(String value) {
		this._snippet = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setOffset(String value) {
		this._offset = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setLexicalVariant(String value) {
		this._lexicalVariant = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteNlpConceptId(Integer value) {
		this._noteNlpConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNlpSystem(String value) {
		this._nlpSystem = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNlpDate(Date value) {
		this._nlpDate = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNlpDatetime(Timestamp value) {
		this._nlpDatetime = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setTermExists(String value) {
		this._termExists = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setTermTemporal(String value) {
		this._termTemporal = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setTermModifiers(String value) {
		this._termModifiers = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setNoteNlpSourceConceptId(Integer value) {
		this._noteNlpSourceConceptId = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setCustomCode(Integer value) {
		this._customCode = value;
		this.setChanged();
		this.notifyObservers();
	}
	
	
	// Fluent setters
	// ==============
	
	public NoteNlpFluent withNoteNlpId(Long value) {
		setNoteNlpId(value);
		return this;
	}
	public NoteNlpFluent withNoteId(Long value) {
		setNoteId(value);
		return this;
	}
	public NoteNlpFluent withSectionConceptId(Integer value) {
		setSectionConceptId(value);
		return this;
	}
	public NoteNlpFluent withSnippet(String value) {
		setSnippet(value);
		return this;
	}
	public NoteNlpFluent withOffset(String value) {
		setOffset(value);
		return this;
	}
	public NoteNlpFluent withLexicalVariant(String value) {
		setLexicalVariant(value);
		return this;
	}
	public NoteNlpFluent withNoteNlpConceptId(Integer value) {
		setNoteNlpConceptId(value);
		return this;
	}
	public NoteNlpFluent withNlpSystem(String value) {
		setNlpSystem(value);
		return this;
	}
	public NoteNlpFluent withNlpDate(Date value) {
		setNlpDate(value);
		return this;
	}
	public NoteNlpFluent withNlpDatetime(Timestamp value) {
		setNlpDatetime(value);
		return this;
	}
	public NoteNlpFluent withTermExist(String value) {
		setTermExists(value);
		return this;
	}
	public NoteNlpFluent withTermTemporal(String value) {
		setTermTemporal(value);
		return this;
	}
	public NoteNlpFluent withTermModifier(String value) {
		setTermModifiers(value);
		return this;
	}
	public NoteNlpFluent withNoteNlpSourceConceptId(Integer value) {
		setNoteNlpSourceConceptId(value);
		return this;
	}
	public NoteNlpFluent withCustomCode(Integer value) {
		setCustomCode(value);
		return this;
	}

	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getNoteNlpId() == null) ? 0 : getNoteNlpId().hashCode());
		result = prime * result + ((getNoteId() == null) ? 0 : getNoteId().hashCode());
		result = prime * result + ((getSectionConceptId() == null) ? 0 : getSectionConceptId().hashCode());
		result = prime * result + ((getSnippet() == null) ? 0 : getSnippet().hashCode());
		result = prime * result + ((getOffset() == null) ? 0 : getOffset().hashCode());
		result = prime * result + ((getLexicalVariant() == null) ? 0 : getLexicalVariant().hashCode());
		result = prime * result + ((getNoteNlpConceptId() == null) ? 0 : getNoteNlpConceptId().hashCode());
		result = prime * result + ((getNlpSystem() == null) ? 0 : getNlpSystem().hashCode());
		result = prime * result + ((getNlpDate() == null) ? 0 : getNlpDate().hashCode());
		result = prime * result + ((getNlpDatetime() == null) ? 0 : getNlpDatetime().hashCode());
		result = prime * result + ((getTermExists() == null) ? 0 : getTermExists().hashCode());
		result = prime * result + ((getTermTemporal() == null) ? 0 : getTermTemporal().hashCode());
		result = prime * result + ((getTermModifiers() == null) ? 0 : getTermModifiers().hashCode());
		result = prime * result + ((getNoteNlpSourceConceptId() == null) ? 0 : getNoteNlpSourceConceptId().hashCode());
		result = prime * result + ((getCustomCode() == null) ? 0 : getCustomCode().hashCode());
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
		NoteNlpFluentImpl other = (NoteNlpFluentImpl) obj;
		//testing this.getNoteNlpId()
		if (this.getNoteNlpId() == null ^ other.getNoteNlpId()==null) return false;
		if (this.getNoteNlpId() != null && other.getNoteNlpId()!=null) {
			if (!this.getNoteNlpId().equals(other.getNoteNlpId())) return false;
		}
		//testing this.getNoteId()
		if (this.getNoteId() == null ^ other.getNoteId()==null) return false;
		if (this.getNoteId() != null && other.getNoteId()!=null) {
			if (!this.getNoteId().equals(other.getNoteId())) return false;
		}
		//testing this.getSectionConceptId()
		if (this.getSectionConceptId() == null ^ other.getSectionConceptId()==null) return false;
		if (this.getSectionConceptId() != null && other.getSectionConceptId()!=null) {
			if (!this.getSectionConceptId().equals(other.getSectionConceptId())) return false;
		}
		//testing this.getSnippet()
		if (this.getSnippet() == null ^ other.getSnippet()==null) return false;
		if (this.getSnippet() != null && other.getSnippet()!=null) {
			if (!this.getSnippet().equals(other.getSnippet())) return false;
		}
		//testing this.getOffset()
		if (this.getOffset() == null ^ other.getOffset()==null) return false;
		if (this.getOffset() != null && other.getOffset()!=null) {
			if (!this.getOffset().equals(other.getOffset())) return false;
		}
		//testing this.getLexicalVariant()
		if (this.getLexicalVariant() == null ^ other.getLexicalVariant()==null) return false;
		if (this.getLexicalVariant() != null && other.getLexicalVariant()!=null) {
			if (!this.getLexicalVariant().equals(other.getLexicalVariant())) return false;
		}
		//testing this.getNoteNlpConceptId()
		if (this.getNoteNlpConceptId() == null ^ other.getNoteNlpConceptId()==null) return false;
		if (this.getNoteNlpConceptId() != null && other.getNoteNlpConceptId()!=null) {
			if (!this.getNoteNlpConceptId().equals(other.getNoteNlpConceptId())) return false;
		}
		//testing this.getNlpSystem()
		if (this.getNlpSystem() == null ^ other.getNlpSystem()==null) return false;
		if (this.getNlpSystem() != null && other.getNlpSystem()!=null) {
			if (!this.getNlpSystem().equals(other.getNlpSystem())) return false;
		}
		//testing this.getNlpDate()
		if (this.getNlpDate() == null ^ other.getNlpDate()==null) return false;
		if (this.getNlpDate() != null && other.getNlpDate()!=null) {
			if (!this.getNlpDate().equals(other.getNlpDate())) return false;
		}
		//testing this.getNlpDatetime()
		if (this.getNlpDatetime() == null ^ other.getNlpDatetime()==null) return false;
		if (this.getNlpDatetime() != null && other.getNlpDatetime()!=null) {
			if (!this.getNlpDatetime().equals(other.getNlpDatetime())) return false;
		}
		//testing this.getTermExists()
		if (this.getTermExists() == null ^ other.getTermExists()==null) return false;
		if (this.getTermExists() != null && other.getTermExists()!=null) {
			if (!this.getTermExists().equals(other.getTermExists())) return false;
		}
		//testing this.getTermTemporal()
		if (this.getTermTemporal() == null ^ other.getTermTemporal()==null) return false;
		if (this.getTermTemporal() != null && other.getTermTemporal()!=null) {
			if (!this.getTermTemporal().equals(other.getTermTemporal())) return false;
		}
		//testing this.getTermModifiers()
		if (this.getTermModifiers() == null ^ other.getTermModifiers()==null) return false;
		if (this.getTermModifiers() != null && other.getTermModifiers()!=null) {
			if (!this.getTermModifiers().equals(other.getTermModifiers())) return false;
		}
		//testing this.getNoteNlpSourceConceptId()
		if (this.getNoteNlpSourceConceptId() == null ^ other.getNoteNlpSourceConceptId()==null) return false;
		if (this.getNoteNlpSourceConceptId() != null && other.getNoteNlpSourceConceptId()!=null) {
			if (!this.getNoteNlpSourceConceptId().equals(other.getNoteNlpSourceConceptId())) return false;
		}
		//testing this.getCustomCode()
		if (this.getCustomCode() == null ^ other.getCustomCode()==null) return false;
		if (this.getCustomCode() != null && other.getCustomCode()!=null) {
			if (!this.getCustomCode().equals(other.getCustomCode())) return false;
		}
		return true;
	}
	
}
