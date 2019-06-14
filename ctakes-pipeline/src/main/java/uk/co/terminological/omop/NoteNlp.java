package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.NoteNlp;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.note_nlp")
public interface NoteNlp  {

	@Column(isAutoIncrement=true, isNullable=false, jdbcType=JDBCType.BIGINT, name="note_nlp_id", length=19)
	public Long getNoteNlpId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="note_id", length=19)
	public Long getNoteId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="section_concept_id", length=10)
	public Integer getSectionConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="snippet", length=250)
	public String getSnippet();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="offset", length=250)
	public String getOffset();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.VARCHAR, name="lexical_variant", length=250)
	public String getLexicalVariant();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="note_nlp_concept_id", length=10)
	public Integer getNoteNlpConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_system", length=250)
	public String getNlpSystem();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.DATE, name="nlp_date", length=10)
	public Date getNlpDate();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.TIMESTAMP, name="nlp_datetime", length=27)
	public Timestamp getNlpDatetime();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="term_exists", length=1)
	public String getTermExists();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="term_temporal", length=50)
	public String getTermTemporal();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="term_modifiers", length=2000)
	public String getTermModifiers();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="note_nlp_source_concept_id", length=10)
	public Integer getNoteNlpSourceConceptId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="custom_code", length=10)
	public Integer getCustomCode();
	
}
