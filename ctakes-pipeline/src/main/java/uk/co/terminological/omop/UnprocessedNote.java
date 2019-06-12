package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Query;

import uk.co.terminological.omop.UnprocessedNote;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Query(sql="SELECT n.*  FROM omopBuild.dbo.IdentifiableNote n LEFT OUTER JOIN omop.dbo.note_nlp nlp ON n.note_id = nlp.note_id  WHERE nlp.note_id IS NULL ORDER BY n.note_id OFFSET 0 ROWS FETCH FIRST 100 ROWS ONLY;", parameterTypes={})
public interface UnprocessedNote  {

	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="note_id", length=19)
	public Long getNoteId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="note_event_id", length=19)
	public Long getNoteEventId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="note_event_field_concept_id", length=10)
	public Integer getNoteEventFieldConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DATE, name="note_date", length=10)
	public Date getNoteDate();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="note_datetime", length=27)
	public Timestamp getNoteDatetime();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="note_type_concept_id", length=10)
	public Integer getNoteTypeConceptId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="note_class_concept_id", length=10)
	public Integer getNoteClassConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="note_title", length=250)
	public String getNoteTitle();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="note_text", length=2147483647)
	public String getNoteText();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="encoding_concept_id", length=10)
	public Integer getEncodingConceptId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="language_concept_id", length=10)
	public Integer getLanguageConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="note_source_value", length=50)
	public String getNoteSourceValue();
	@Id
	public Integer getRowNumber();
	
}
