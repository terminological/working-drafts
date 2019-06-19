package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ProcedureOccurrenceSql extends ProcedureOccurrenceFluentImpl implements ProcedureOccurrence {


	// Public constructor
	// ==================

	public ProcedureOccurrenceSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("procedure_occurrence_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("procedure_concept_id",Integer.class),
			resultSet.getObject("procedure_date",Date.class),
			resultSet.getObject("procedure_datetime",Timestamp.class),
			resultSet.getObject("procedure_type_concept_id",Integer.class),
			resultSet.getObject("modifier_concept_id",Integer.class),
			resultSet.getObject("quantity",Integer.class),
			resultSet.getObject("provider_id",Long.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("procedure_source_value",String.class),
			resultSet.getObject("procedure_source_concept_id",Integer.class),
			resultSet.getObject("modifier_source_value",String.class)			
		);
	}
	
}
