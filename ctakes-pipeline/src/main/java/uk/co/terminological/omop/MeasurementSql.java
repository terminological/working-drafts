package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class MeasurementSql extends MeasurementFluentImpl implements Measurement {


	// Public constructor
	// ==================

	public MeasurementSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("measurement_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("measurement_concept_id",Integer.class),
			resultSet.getObject("measurement_date",Date.class),
			resultSet.getObject("measurement_datetime",Timestamp.class),
			resultSet.getObject("measurement_time",String.class),
			resultSet.getObject("measurement_type_concept_id",Integer.class),
			resultSet.getObject("operator_concept_id",Integer.class),
			resultSet.getObject("value_as_number",Double.class),
			resultSet.getObject("value_as_concept_id",Integer.class),
			resultSet.getObject("unit_concept_id",Integer.class),
			resultSet.getObject("range_low",Double.class),
			resultSet.getObject("range_high",Double.class),
			resultSet.getObject("provider_id",Long.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("measurement_source_value",String.class),
			resultSet.getObject("measurement_source_concept_id",Integer.class),
			resultSet.getObject("unit_source_value",String.class),
			resultSet.getObject("value_source_value",String.class)			
		);
	}
	
}
