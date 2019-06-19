package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class CuiOmopMapSql extends CuiOmopMapFluentImpl implements CuiOmopMap {


	// Public constructor
	// ==================

	public CuiOmopMapSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("CUI",String.class),
			resultSet.getObject("source_concept_id",Integer.class),
			resultSet.getObject("concept_id",Integer.class)			
		);
	}
	
}
