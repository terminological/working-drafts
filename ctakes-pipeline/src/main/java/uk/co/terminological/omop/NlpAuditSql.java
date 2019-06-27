package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class NlpAuditSql extends NlpAuditFluentImpl implements NlpAudit {


	// Public constructor
	// ==================

	public NlpAuditSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("note_id",Long.class),
			resultSet.getObject("event_time",Timestamp.class),
			resultSet.getObject("nlp_system",String.class),
			resultSet.getObject("nlp_system_instance",String.class),
			resultSet.getObject("event_type",String.class),
			resultSet.getObject("event_detail",String.class),
			resultSet.getObject("priority",Integer.class)			
		);
	}
	
}
