package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.ProcedureOccurrence;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.procedure_occurrence")
public interface ProcedureOccurrence  {

	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="procedure_occurrence_id", length=19)
	public Long getProcedureOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="procedure_concept_id", length=10)
	public Integer getProcedureConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.DATE, name="procedure_date", length=10)
	public Date getProcedureDate();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.TIMESTAMP, name="procedure_datetime", length=27)
	public Timestamp getProcedureDatetime();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="procedure_type_concept_id", length=10)
	public Integer getProcedureTypeConceptId();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="modifier_concept_id", length=10)
	public Integer getModifierConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.INTEGER, name="quantity", length=10)
	public Integer getQuantity();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="procedure_source_value", length=50)
	public String getProcedureSourceValue();
	@Column(isAutoIncrement=false, isNullable=false, jdbcType=JDBCType.INTEGER, name="procedure_source_concept_id", length=10)
	public Integer getProcedureSourceConceptId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="modifier_source_value", length=50)
	public String getModifierSourceValue();
	
}
