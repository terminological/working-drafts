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
@Query(sql="SELECT TOP(1) * from omopBuild.dbo.NlpWorklist where \t\t\t\t\t\t\t\t\t\t\t\tnlp_system=? ORDER BY nlp_priority, nlp_event_time", parameterTypes={java.lang.String.class})
public interface Input  {

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
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_event_type", length=20)
	public String getNlpEventType();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_system", length=250)
	public String getNlpSystem();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_system_instance", length=250)
	public String getNlpSystemInstance();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="nlp_event_time", length=23)
	public Timestamp getNlpEventTime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_event_detail", length=512)
	public String getNlpEventDetail();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="nlp_priority", length=10)
	public Integer getNlpPriority();
	@Id
	public Integer getRowNumber();
	
}
