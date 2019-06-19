package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ObservationSql extends ObservationFluentImpl implements Observation {


	// Public constructor
	// ==================

	public ObservationSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("observation_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("observation_concept_id",Integer.class),
			resultSet.getObject("observation_date",Date.class),
			resultSet.getObject("observation_datetime",Timestamp.class),
			resultSet.getObject("observation_type_concept_id",Integer.class),
			resultSet.getObject("value_as_number",Double.class),
			resultSet.getObject("value_as_string",String.class),
			resultSet.getObject("value_as_concept_id",Integer.class),
			resultSet.getObject("qualifier_concept_id",Integer.class),
			resultSet.getObject("unit_concept_id",Integer.class),
			resultSet.getObject("provider_id",Integer.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("observation_source_value",String.class),
			resultSet.getObject("observation_source_concept_id",Integer.class),
			resultSet.getObject("unit_source_value",String.class),
			resultSet.getObject("qualifier_source_value",String.class),
			resultSet.getObject("observation_event_id",Long.class),
			resultSet.getObject("obs_event_field_concept_id",Integer.class),
			resultSet.getObject("value_as_datetime",Timestamp.class)			
		);
	}
	
}
