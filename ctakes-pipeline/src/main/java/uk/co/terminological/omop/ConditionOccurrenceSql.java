package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ConditionOccurrenceSql extends ConditionOccurrenceFluentImpl implements ConditionOccurrence {


	// Public constructor
	// ==================

	public ConditionOccurrenceSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("condition_occurrence_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("condition_concept_id",Integer.class),
			resultSet.getObject("condition_start_date",Date.class),
			resultSet.getObject("condition_start_datetime",Timestamp.class),
			resultSet.getObject("condition_end_date",Date.class),
			resultSet.getObject("condition_end_datetime",Timestamp.class),
			resultSet.getObject("condition_type_concept_id",Integer.class),
			resultSet.getObject("condition_status_concept_id",Integer.class),
			resultSet.getObject("stop_reason",String.class),
			resultSet.getObject("provider_id",Long.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("condition_source_value",String.class),
			resultSet.getObject("condition_source_concept_id",Integer.class),
			resultSet.getObject("condition_status_source_value",String.class)			
		);
	}
	
}
