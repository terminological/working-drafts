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

	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.BIGINT, name="note_id", length=19)
	public Long getNoteId();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="event_time", length=23)
	public Timestamp getEventTime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_system", length=250)
	public String getNlpSystem();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="nlp_system_instance", length=250)
	public String getNlpSystemInstance();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="event_type", length=20)
	public String getEventType();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="event_detail", length=512)
	public String getEventDetail();
	
}
