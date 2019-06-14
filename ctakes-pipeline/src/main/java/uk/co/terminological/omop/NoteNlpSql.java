package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class NoteNlpSql extends NoteNlpFluentImpl implements NoteNlp {


	// Public constructor
	// ==================

	public NoteNlpSql(ResultSet resultSet) throws SQLException {
		super(
			resultSet.getObject("note_nlp_id",Long.class),
			resultSet.getObject("note_id",Long.class),
			resultSet.getObject("section_concept_id",Integer.class),
			resultSet.getObject("snippet",String.class),
			resultSet.getObject("offset",String.class),
			resultSet.getObject("lexical_variant",String.class),
			resultSet.getObject("note_nlp_concept_id",Integer.class),
			resultSet.getObject("nlp_system",String.class),
			resultSet.getObject("nlp_date",Date.class),
			resultSet.getObject("nlp_datetime",Timestamp.class),
			resultSet.getObject("term_exists",String.class),
			resultSet.getObject("term_temporal",String.class),
			resultSet.getObject("term_modifiers",String.class),
			resultSet.getObject("note_nlp_source_concept_id",Integer.class),
			resultSet.getObject("custom_code",Integer.class)			
		);
	}
	
}
