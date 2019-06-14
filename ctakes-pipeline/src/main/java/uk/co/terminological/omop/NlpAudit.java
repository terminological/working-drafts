package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.omop.NlpAudit;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(schema="", name="omopBuild.dbo.NlpAudit")
public interface NlpAudit  {

	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.BIGINT, name="note_id", length=19)
	public Long getNoteId();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.TIMESTAMP, name="event_time", length=23)
	public Timestamp getEventTime();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_system", length=250)
	public String getNlpSystem();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="nlp_system_instance", length=250)
	public String getNlpSystemInstance();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="event_type", length=20)
	public String getEventType();
	@Column(isAutoIncrement=false, isNullable=true, jdbcType=JDBCType.VARCHAR, name="event_detail", length=2147483647)
	public String getEventDetail();
	
}
