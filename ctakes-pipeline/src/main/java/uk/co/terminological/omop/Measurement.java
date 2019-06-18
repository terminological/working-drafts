package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.Measurement;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.measurement")
public interface Measurement  {

	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="measurement_id", length=19)
	public Long getMeasurementId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="measurement_concept_id", length=10)
	public Integer getMeasurementConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DATE, name="measurement_date", length=10)
	public Date getMeasurementDate();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="measurement_datetime", length=27)
	public Timestamp getMeasurementDatetime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="measurement_time", length=10)
	public String getMeasurementTime();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="measurement_type_concept_id", length=10)
	public Integer getMeasurementTypeConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="operator_concept_id", length=10)
	public Integer getOperatorConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DOUBLE, name="value_as_number", length=15)
	public Double getValueAsNumber();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="value_as_concept_id", length=10)
	public Integer getValueAsConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="unit_concept_id", length=10)
	public Integer getUnitConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DOUBLE, name="range_low", length=15)
	public Double getRangeLow();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DOUBLE, name="range_high", length=15)
	public Double getRangeHigh();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="measurement_source_value", length=50)
	public String getMeasurementSourceValue();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="measurement_source_concept_id", length=10)
	public Integer getMeasurementSourceConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="unit_source_value", length=50)
	public String getUnitSourceValue();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="value_source_value", length=50)
	public String getValueSourceValue();
	
}
