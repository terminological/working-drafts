package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class UnprocessedNoteSql extends UnprocessedNoteFluentImpl implements UnprocessedNote {


	// Public constructor
	// ==================

	public UnprocessedNoteSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("note_id",Long.class),
			resultSet.getObject("person_id",Long.class),
			resultSet.getObject("note_event_id",Long.class),
			resultSet.getObject("note_event_field_concept_id",Integer.class),
			resultSet.getObject("note_date",Date.class),
			resultSet.getObject("note_datetime",Timestamp.class),
			resultSet.getObject("note_type_concept_id",Integer.class),
			resultSet.getObject("note_class_concept_id",Integer.class),
			resultSet.getObject("note_title",String.class),
			resultSet.getObject("note_text",String.class),
			resultSet.getObject("encoding_concept_id",Integer.class),
			resultSet.getObject("language_concept_id",Integer.class),
			resultSet.getObject("provider_id",Long.class),
			resultSet.getObject("visit_occurrence_id",Long.class),
			resultSet.getObject("visit_detail_id",Long.class),
			resultSet.getObject("note_source_value",String.class)			,
			resultSet.getRow()
		);
	}
	
}
