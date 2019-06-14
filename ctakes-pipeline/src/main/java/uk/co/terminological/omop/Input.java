package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Query;

import uk.co.terminological.omop.Input;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Query(sql="SELECT TOP(1) * from omopBuild.dbo.NlpWorklist where nlp_system=?", parameterTypes={java.lang.String.class})
public interface Input  {

	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="note_id", length=19)
	public Long getNoteId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="note_event_id", length=19)
	public Long getNoteEventId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="note_event_field_concept_id", length=10)
	public Integer getNoteEventFieldConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DATE, name="note_date", length=10)
	public Date getNoteDate();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.TIMESTAMP, name="note_datetime", length=27)
	public Timestamp getNoteDatetime();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="note_type_concept_id", length=10)
	public Integer getNoteTypeConceptId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="note_class_concept_id", length=10)
	public Integer getNoteClassConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="note_title", length=250)
	public String getNoteTitle();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="note_text", length=2147483647)
	public String getNoteText();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="encoding_concept_id", length=10)
	public Integer getEncodingConceptId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="language_concept_id", length=10)
	public Integer getLanguageConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="note_source_value", length=50)
	public String getNoteSourceValue();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_event_type", length=20)
	public String getNlpEventType();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_system", length=250)
	public String getNlpSystem();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_system_instance", length=250)
	public String getNlpSystemInstance();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.TIMESTAMP, name="nlp_event_time", length=23)
	public Timestamp getNlpEventTime();
	@Id
	public Integer getRowNumber();
	
}
