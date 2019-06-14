package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.Observation;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.observation")
public interface Observation  {

	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="observation_id", length=19)
	public Long getObservationId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="observation_concept_id", length=10)
	public Integer getObservationConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DATE, name="observation_date", length=10)
	public Date getObservationDate();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.TIMESTAMP, name="observation_datetime", length=27)
	public Timestamp getObservationDatetime();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="observation_type_concept_id", length=10)
	public Integer getObservationTypeConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DOUBLE, name="value_as_number", length=15)
	public Double getValueAsNumber();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="value_as_string", length=60)
	public String getValueAsString();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.INTEGER, name="value_as_concept_id", length=10)
	public Integer getValueAsConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.INTEGER, name="qualifier_concept_id", length=10)
	public Integer getQualifierConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.INTEGER, name="unit_concept_id", length=10)
	public Integer getUnitConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.INTEGER, name="provider_id", length=10)
	public Integer getProviderId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="observation_source_value", length=50)
	public String getObservationSourceValue();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="observation_source_concept_id", length=10)
	public Integer getObservationSourceConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="unit_source_value", length=50)
	public String getUnitSourceValue();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="qualifier_source_value", length=50)
	public String getQualifierSourceValue();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="observation_event_id", length=19)
	public Long getObservationEventId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="obs_event_field_concept_id", length=10)
	public Integer getObsEventFieldConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.TIMESTAMP, name="value_as_datetime", length=27)
	public Timestamp getValueAsDatetime();
	
}
