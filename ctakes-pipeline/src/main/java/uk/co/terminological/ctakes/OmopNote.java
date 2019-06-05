package uk.co.terminological.ctakes;
import java.util.Date;
public class OmopNote {

	public static enum Fields implements Typed.Reader {
		note_id(Integer.class), //Yes	integer	A unique identifier for each note.
		person_id(Integer.class), //Yes	integer	A foreign key identifier to the Person about whom the Note was recorded. The demographic details of that Person are stored in the PERSON table.
		note_event_id(Integer.class), //No	integer	A foreign key identifier to the event (e.g. Measurement, Procedure, Visit, Drug Exposure, etc) record during which the note was recorded.
		note_event_field_concept_id(Integer.class), //No	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the field to which the note_event_id is referring.
		note_date(Date.class), //No	date	The date the note was recorded.
		note_datetime(Date.class), //Yes	datetime	The date and time the note was recorded.
		note_type_concept_id(Integer.class), //Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the type, origin or provenance of the Note. These belong to the 'Note Type' vocabulary
		note_class_concept_id(Integer.class), //Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the HL7 LOINC Document Type Vocabulary classification of the note.
		note_title(String.class), //No	varchar(250)	The title of the Note as it appears in the source.
		note_text(String.class), //Yes	varchar(MAX)	The content of the Note.
		encoding_concept_id(Integer.class), //Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the note character encoding type
		language_concept_id(Integer.class), //Yes	integer	A foreign key to the predefined Concept in the Standardized Vocabularies reflecting the language of the note
		provider_id(Integer.class), //No	integer	A foreign key to the Provider in the PROVIDER table who took the Note.
		visit_occurrence_id(Integer.class), //No	integer	A foreign key to the Visit in the VISIT_OCCURRENCE table when the Note was taken.
		visit_detail_id(Integer.class), //No	integer	A foreign key to the Visit in the VISIT_DETAIL table when the Note was taken.
		note_source_value(String.class), //No	varchar(50)	The source value associated with the origin of the Note
		;

		private Class<?> type;
		Fields(Class<?> type) {this.type= type;}
		
		@Override
		public Class<?> getType() {
			return type;
		}
		
	}
	
}
