package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class DrugExposureSql extends DrugExposureFluentImpl implements DrugExposure {


	// Public constructor
	// ==================

	public DrugExposureSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("drug_exposure_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("drug_concept_id",Integer.class),
			resultSet.getObject("drug_exposure_start_date",Date.class),
			resultSet.getObject("drug_exposure_start_datetime",Timestamp.class),
			resultSet.getObject("drug_exposure_end_date",Date.class),
			resultSet.getObject("drug_exposure_end_datetime",Timestamp.class),
			resultSet.getObject("verbatim_end_date",Date.class),
			resultSet.getObject("drug_type_concept_id",Integer.class),
			resultSet.getObject("stop_reason",String.class),
			resultSet.getObject("refills",Integer.class),
			resultSet.getObject("quantity",Double.class),
			resultSet.getObject("days_supply",Integer.class),
			resultSet.getObject("sig",String.class),
			resultSet.getObject("route_concept_id",Integer.class),
			resultSet.getObject("lot_number",String.class),
			resultSet.getObject("provider_id",Long.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("drug_source_value",String.class),
			resultSet.getObject("drug_source_concept_id",Integer.class),
			resultSet.getObject("route_source_value",String.class),
			resultSet.getObject("dose_unit_source_value",String.class)			
		);
	}
	
}
