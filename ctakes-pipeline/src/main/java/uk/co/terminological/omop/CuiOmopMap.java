package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.JDBCType;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.CuiOmopMap;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omopBuild.dbo.CuiOmopMap")
public interface CuiOmopMap  {

	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.CHAR, name="CUI", length=8)
	public String getCui();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="source_concept_id", length=10)
	public Integer getSourceConceptId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="concept_id", length=10)
	public Integer getConceptId();
	
}
