package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.DrugExposure;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omop.dbo.drug_exposure")
public interface DrugExposure  {

	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="drug_exposure_id", length=19)
	public Long getDrugExposureId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="person_id", length=19)
	public Long getPersonId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="drug_concept_id", length=10)
	public Integer getDrugConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DATE, name="drug_exposure_start_date", length=10)
	public Date getDrugExposureStartDate();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="drug_exposure_start_datetime", length=27)
	public Timestamp getDrugExposureStartDatetime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DATE, name="drug_exposure_end_date", length=10)
	public Date getDrugExposureEndDate();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="drug_exposure_end_datetime", length=27)
	public Timestamp getDrugExposureEndDatetime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DATE, name="verbatim_end_date", length=10)
	public Date getVerbatimEndDate();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="drug_type_concept_id", length=10)
	public Integer getDrugTypeConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="stop_reason", length=20)
	public String getStopReason();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="refills", length=10)
	public Integer getRefills();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.DOUBLE, name="quantity", length=15)
	public Double getQuantity();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="days_supply", length=10)
	public Integer getDaysSupply();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="sig", length=2147483647)
	public String getSig();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="route_concept_id", length=10)
	public Integer getRouteConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="lot_number", length=50)
	public String getLotNumber();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="provider_id", length=19)
	public Long getProviderId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_occurrence_id", length=19)
	public Long getVisitOccurrenceId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="visit_detail_id", length=19)
	public Long getVisitDetailId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="drug_source_value", length=50)
	public String getDrugSourceValue();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="drug_source_concept_id", length=10)
	public Integer getDrugSourceConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="route_source_value", length=50)
	public String getRouteSourceValue();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="dose_unit_source_value", length=50)
	public String getDoseUnitSourceValue();
	
}
