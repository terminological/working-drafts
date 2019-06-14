package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.ConditionOccurrence;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.condition_occurrence")
public interface ConditionOccurrence  {

	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="condition_occurrence_id", length=19)
	public Long getConditionOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="condition_concept_id", length=10)
	public Integer getConditionConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DATE, name="condition_start_date", length=10)
	public Date getConditionStartDate();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.TIMESTAMP, name="condition_start_datetime", length=27)
	public Timestamp getConditionStartDatetime();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DATE, name="condition_end_date", length=10)
	public Date getConditionEndDate();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.TIMESTAMP, name="condition_end_datetime", length=27)
	public Timestamp getConditionEndDatetime();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="condition_type_concept_id", length=10)
	public Integer getConditionTypeConceptId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="condition_status_concept_id", length=10)
	public Integer getConditionStatusConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="stop_reason", length=20)
	public String getStopReason();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="condition_source_value", length=50)
	public String getConditionSourceValue();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="condition_source_concept_id", length=10)
	public Integer getConditionSourceConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="condition_status_source_value", length=50)
	public String getConditionStatusSourceValue();
	
}
