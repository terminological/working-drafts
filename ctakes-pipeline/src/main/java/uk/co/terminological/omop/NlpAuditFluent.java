package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.util.*;
import uk.co.terminological.omop.NlpAudit;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface NlpAuditFluent extends NlpAudit {

	// POJO setters
	// ==============

	public void setNoteId(Long value);

	public void setEventTime(Timestamp value);

	public void setNlpSystem(String value);

	public void setNlpSystemInstance(String value);

	public void setEventType(String value);

	public void setEventDetail(String value);

	public void setPriority(Integer value);

	
	// Fluent setters
	// ==============
	
	public NlpAuditFluent withNoteId(Long value);
	
	public NlpAuditFluent withEventTime(Timestamp value);
	
	public NlpAuditFluent withNlpSystem(String value);
	
	public NlpAuditFluent withNlpSystemInstance(String value);
	
	public NlpAuditFluent withEventType(String value);
	
	public NlpAuditFluent withEventDetail(String value);
	
	public NlpAuditFluent withPriority(Integer value);
	
}
