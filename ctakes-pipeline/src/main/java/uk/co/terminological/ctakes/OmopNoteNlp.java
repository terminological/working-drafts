package uk.co.terminological.ctakes;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import com.j256.ormlite.field.DataType;

@DatabaseTable(tableName = "note_nlp")
public class OmopNoteNlp {
		
	@DatabaseField(generatedId = true, columnName="note_nlp_id") 
	private Long noteNlpId;	//Yes	integer	A unique identifier for each term extracted from a note.
	
	@DatabaseField(columnName = "note_id", canBeNull = false) 
	private Long noteId;	//Yes	integer	A foreign key to the Note table note the term was
	
	@DatabaseField(columnName = "section_concept_id", canBeNull = false) 
	private Integer sectionConceptId;	//	Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies representing the section of the extracted term.
	
	@DatabaseField(columnName = "snippet", canBeNull = false) 
	private String snippet;	//	No	varchar(250)	A small window of text surrounding the term.
	
	@DatabaseField(columnName = "offset") 
	private String offset;	//	No	varchar(50)	Character offset of the extracted term in the input note.
	
	@DatabaseField(columnName = "lexical_variant", canBeNull = false) 
	private String lexicalVariant;	//	Yes	varchar(250)	Raw text extracted from the NLP tool.
	
	@DatabaseField(columnName = "note_nlp_concept_id", canBeNull = false) 
	private Integer noteNlpConceptId;	//	Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the normalized concept for the extracted term. Domain of the term is represented as part of the Concept table.
	
	@DatabaseField(columnName = "note_nlp_source_concept_id", canBeNull = false) 
	private Integer noteNlpSourceConceptId;	//	Yes	integer	A foreign key to a Concept that refers to the code in the source vocabulary used by the NLP system
	
	@DatabaseField(columnName = "nlp_system") 
	private String nlpSystem;	//	No	varchar(250)	Name and version of the NLP system that extracted the term.Useful for data provenance.
	
	@DatabaseField(columnName = "nlp_date", canBeNull = false, dataType = DataType.DATE) 
	private Date nlpDate;	//	Yes	date	The date of the note processing.Useful for data provenance.
	
	@DatabaseField(columnName = "nlp_datetime", dataType = DataType.DATE_TIME) 
	private Date nlpDatetime;	//	No	datetime	The date and time of the note processing. Useful for data provenance.
	
	@DatabaseField(columnName = "term_exists")
	private String termExists;	//	No	varchar(1)	A summary modifier that signifies presence or absence of the term for a given patient. Useful for quick querying.
	
	@DatabaseField(columnName = "term_temporal") 
	private String termTemporal;	//	No	varchar(50)	An optional time modifier associated with the extracted term. (for now “past” or “present” only). Standardize it later.
	
	@DatabaseField(columnName = "term_modifiers") 
	private String termModifiers;	//	No	varchar(2000)	A compact description of all the modifiers of the specific term extracted by the NLP system. (e.g. “son has rash” ? “negated=no,subject=family, certainty=undef,conditional=false,general=false”).

	/********************* GETTERS AND SETTERS ************************/
	
	public Long getNoteNlpId() {
		return noteNlpId;
	}

	public void setNoteNlpId(Long noteNlpId) {
		this.noteNlpId = noteNlpId;
	}

	public Long getNoteId() {
		return noteId;
	}

	public void setNoteId(Long noteId) {
		this.noteId = noteId;
	}

	public Integer getSectionConceptId() {
		return sectionConceptId;
	}

	public void setSectionConceptId(Integer sectionConceptId) {
		this.sectionConceptId = sectionConceptId;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getLexicalVariant() {
		return lexicalVariant;
	}

	public void setLexicalVariant(String lexicalVariant) {
		this.lexicalVariant = lexicalVariant;
	}

	public Integer getNoteNlpConceptId() {
		return noteNlpConceptId;
	}

	public void setNoteNlpConceptId(Integer noteNlpConceptId) {
		this.noteNlpConceptId = noteNlpConceptId;
	}

	public Integer getNoteNlpSourceConceptId() {
		return noteNlpSourceConceptId;
	}

	public void setNoteNlpSourceConceptId(Integer noteNlpSourceConceptId) {
		this.noteNlpSourceConceptId = noteNlpSourceConceptId;
	}

	public String getNlpSystem() {
		return nlpSystem;
	}

	public void setNlpSystem(String nlpSystem) {
		this.nlpSystem = nlpSystem;
	}

	public Date getNlpDate() {
		return nlpDate;
	}

	public void setNlpDate(Date nlpDate) {
		this.nlpDate = nlpDate;
	}

	public Date getNlpDatetime() {
		return nlpDatetime;
	}

	public void setNlpDatetime(Date nlpDatetime) {
		this.nlpDatetime = nlpDatetime;
	}

	public String getTermExists() {
		return termExists;
	}

	public void setTermExists(String termExists) {
		this.termExists = termExists;
	}

	public String getTermTemporal() {
		return termTemporal;
	}

	public void setTermTemporal(String termTemporal) {
		this.termTemporal = termTemporal;
	}

	public String getTermModifiers() {
		return termModifiers;
	}

	public void setTermModifiers(String termModifiers) {
		this.termModifiers = termModifiers;
	}
	
	
	
	
	
}
