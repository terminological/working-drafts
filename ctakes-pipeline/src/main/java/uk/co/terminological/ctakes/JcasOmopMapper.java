package uk.co.terminological.ctakes;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uk.co.terminological.datatypes.OneToManyMap;
import uk.co.terminological.omop.CuiOmopMap;
import uk.co.terminological.omop.Database;
import uk.co.terminological.omop.DrugExposure;
import uk.co.terminological.omop.Factory;
import uk.co.terminological.omop.Input;
import uk.co.terminological.omop.NoteNlp;
import uk.co.terminological.omop.NoteNlpFluent;
import uk.co.terminological.omop.Observation;
import uk.co.terminological.omop.ProcedureOccurrence;

public class JcasOmopMapper {

	static Logger log = LoggerFactory.getLogger(JcasOmopMapper.class);
	
	private static final Integer PRESENT = 4181412; // from SNOMED
	private static final Integer ABSENT = 4132135; // from SNOMED
	//private static final Integer CONDITION_TYPE_NLP_DERIVED=32424;
	//private static final Integer MEAS_TYPE_NLP_DERIVED=32423;
	private static final Integer PROCEDURE_TYPE_NLP_DERIVED=32425;
	//private static final Integer DRUG_TYPE_NLP_DERIVED=32426;
	private static final  Integer OBSERVATION_TYPE_NLP_DERIVED=32445;
	
	
	//private static final  Integer CONDITION_STATUS_FINAL_DIAGNOSIS=4230359;
	//private static final  Integer SECTION_CONCEPT_CLINICAL_DOCUMENT=4309829; //concept_class_id = 'Record Artifact'
	
	//public static WordVector map(WordToken token) {}
	
	OneToManyMap<String,CuiOmopMap> conceptMapper;
	String version;
	
	Gson gson = new GsonBuilder().create();
	
	CuiOmopMap defaultMap(String CUI) {
		return Factory.Mutable.createCuiOmopMap().withSourceConceptId(0).withConceptId(0).withCui(CUI);
	}
	
	public JcasOmopMapper(Database db, String version) throws SQLException {
		conceptMapper = new OneToManyMap<>();
		db.read().streamCuiOmopMap().forEach(
			com -> conceptMapper.putItem(com.getCui(),com)
		);
		this.version = version;
	}
	
	public Stream<CuiOmopMap> mapConcept(IdentifiedAnnotation jcas) {
		if (jcas == null || jcas.getOntologyConceptArr() == null) return Stream.empty();
		return Stream.of(jcas.getOntologyConceptArr().toArray())
			.filter(fs -> fs instanceof UmlsConcept)
			.map(u -> ((UmlsConcept) u).getCui())
			.distinct()
			.flatMap(cui -> conceptMapper.stream(cui, () -> defaultMap(cui)));
	}
	
	public static boolean isValidAndCurrent(IdentifiedAnnotation jcas) {
		return 
				jcas.getPolarity() == 1 &&
				jcas.getSubject().equals(CONST.ATTR_SUBJECT_PATIENT) && 
				jcas.getHistoryOf() == CONST.NE_HISTORY_OF_ABSENT;
	}
	
	public <X extends IdentifiedAnnotation> Stream<NoteNlp> mapNote(Input note, X jcas) {
		return mapConcept(jcas).map(com -> {
			Integer conceptId = com.getConceptId();
			NoteNlpFluent noteNlp = Factory.Mutable.createNoteNlp()
					.withNoteNlpConceptId(conceptId)
					.withNoteNlpSourceConceptId(conceptId)
					//.withSnippet(note.getNoteTitle())
					.withLexicalVariant(jcas.getCoveredText())
					.withNlpDate(Date.valueOf(LocalDate.now()))
					.withNlpDatetime(Timestamp.valueOf(LocalDateTime.now()))
					.withNlpSystem(version)
					.withNoteId(note.getNoteId())
					.withOffset(""+jcas.getBegin()+"-"+jcas.getEnd())
					.withSectionConceptId(note.getNoteClassConceptId())
					.withTermExist(
							(
								jcas.getPolarity() == 1 &&
								jcas.getSubject().equals(CONST.ATTR_SUBJECT_PATIENT) 
								// conditional
							) ?"Y":"N")
					.withTermTemporal(
							(
								jcas.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT
							) ?"historical":"current")
					.withCustomCode(codeFor(jcas))
					;
			
			Object omopEntry;
			if (!isValidAndCurrent(jcas))
				omopEntry = this.map(note, jcas, com); //Default to just a note_nlp entry 
			else {
				if (jcas instanceof SignSymptomMention)
					omopEntry = this.map(note, (SignSymptomMention) jcas, com);
				else if (jcas instanceof DiseaseDisorderMention)
					omopEntry = this.map(note, (DiseaseDisorderMention) jcas, com);
				else if (jcas instanceof MedicationMention)
					omopEntry = this.map(note, (MedicationMention) jcas, com);
				else if (jcas instanceof ProcedureMention)
					omopEntry = this.map(note, (ProcedureMention) jcas, com);
				else
					omopEntry = this.map(note, jcas, com);
			}
				
			noteNlp.withTermModifier(gson.toJson(omopEntry));
			
			return noteNlp;
		});
	}
	
	public DrugExposure map(Input note, MedicationMention jcas, CuiOmopMap com) {
		Integer conceptId = com.getConceptId();
		DrugExposure out = Factory.Mutable.createDrugExposure()
				.withDrugConceptId(conceptId)
				.withPersonId(note.getPersonId())
				.withDrugExposureStartDate(note.getNoteDate())
				.withDrugExposureStartDatetime(note.getNoteDatetime())
				.withDrugExposureEndDatetime(note.getNoteDatetime())
				.withRouteConceptId(0) //unknown
				.withDrugSourceConceptId(conceptId)
				.withDrugSourceValue(com.getCui()+": "+jcas.getCoveredText());
		return out;
	}
	
	/*public DrugExposure map(UnprocessedNote note, MedicationEventMention jcas) {
		throw new NotImplementedException();
	}*/
	
	public Observation map(Input note, SignSymptomMention jcas, CuiOmopMap com) {
		Integer conceptId = com.getConceptId();
		Observation out = Factory.Mutable.createObservation()
				.withObservationConceptId(conceptId)
				.withPersonId(note.getPersonId())
				.withObservationDate(note.getNoteDate())
				.withObservationDatetime(note.getNoteDatetime())
				.withObservationSourceValue(com.getCui()+": "+jcas.getCoveredText())
				.withObservationSourceConceptId(conceptId)
				.withObservationTypeConceptId(OBSERVATION_TYPE_NLP_DERIVED)
				.withObsEventFieldConceptId(0)
				.withValueAsConceptId(jcas.getPolarity() == 1 ? PRESENT : ABSENT)
				//
				;
		
		
		/* TODO: Qualifiers seem possible
		 * but these are not populated because we are not running correct bit of pipeline
		 * something to do with the 
		 * http://ctakes.apache.org/apidocs/trunk/index.html?org/apache/ctakes/typesystem/type/textsem/IdentifiedAnnotation.html
		 * 
		 StringBuffer sb = new StringBuffer();
		if (jcas.getRelativeTemporalContext() != null) {
			sb.append("TEMPORAL\n");
			sb.append(jcas.getRelativeTemporalContext().getClass().getCanonicalName()+"\n");
		}
		if (jcas.getBodyLocation() != null) {
			sb.append("LOCATION\n");
			sb.append(jcas.getBodyLocation().getArg2().getClass().getCanonicalName()+"\n");
		}
		System.out.println(sb);*/
		return out;
						
	}
	
	//N.B. mapping disease disorder mentions to Observation as not reliable.
	/*public ConditionOccurrence map(UnprocessedNote note, DiseaseDisorderMention jcas) {
	Integer conceptId = mapConcept(jcas).getConceptId();
	ConditionOccurrenceFluent out = 
			Factory.Mutable.createConditionOccurrence()
				.withConditionConceptId(conceptId)
				.withPersonId(note.getPersonId())
				//TODO: is there anything we can do for things that are non temporal?
				.withConditionStartDate(note.getNoteDate())
				.withConditionStartDatetime(note.getNoteDatetime())
				
				.withConditionTypeConceptId(CONDITION_TYPE_NLP_DERIVED)
				.withConditionSourceConceptId(conceptId)
				.withConditionSourceValue(jcas.getCoveredText())
				.withConditionStatusConceptId(CONDITION_STATUS_FINAL_DIAGNOSIS)
				.withConditionStatusSourceValue(note.getNoteSourceValue());
	return out;
	}*/


	
	public Observation map(Input note, DiseaseDisorderMention jcas, CuiOmopMap com) {
		
		Integer conceptId = com.getConceptId();
		Observation out = Factory.Mutable.createObservation()
				.withObservationConceptId(conceptId)
				.withPersonId(note.getPersonId())
				.withObservationDate(note.getNoteDate())
				.withObservationDatetime(note.getNoteDatetime())
				.withObservationSourceValue(com.getCui()+": "+jcas.getCoveredText())
				.withObservationSourceConceptId(conceptId)
				.withObservationTypeConceptId(OBSERVATION_TYPE_NLP_DERIVED)
				.withObsEventFieldConceptId(0)
				.withValueAsConceptId(jcas.getPolarity() == 1 ? PRESENT : ABSENT);
		return out;
		
	}
	
	public ProcedureOccurrence map(Input note, ProcedureMention jcas, CuiOmopMap com) {
		Integer conceptId = com.getConceptId();
		ProcedureOccurrence out = Factory.Mutable.createProcedureOccurrence()
				.withPersonId(note.getPersonId())
				.withProcedureDate(note.getNoteDate())
				.withProcedureDatetime(note.getNoteDatetime())
				.withProcedureSourceValue(com.getCui()+": "+jcas.getCoveredText())
				.withProcedureSourceConceptId(conceptId)
				.withProcedureTypeConceptId(PROCEDURE_TYPE_NLP_DERIVED)
				.withModifierConceptId(mapConcept(jcas.getBodyLaterality()).findFirst().orElse(defaultMap(com.getCui())).getConceptId()); //defaults to 0
		return out;
	}
	
	/*public Measurement map(UnprocessedNote note, LabMention jcas) {
		throw new NotImplementedException();
	}*/
	
	public Unspecified map(Input note, IdentifiedAnnotation entity, CuiOmopMap com) {
		Unspecified out = new Unspecified();
		out.cui=com.getCui();
		out.type=entity.getClass().getCanonicalName();
		out.begin=entity.getBegin();
		out.end=entity.getEnd();
		out.coveredText=entity.getCoveredText();
		out.polarity=entity.getPolarity();
		out.isUncertain=(entity.getUncertainty() == CONST.NE_UNCERTAINTY_PRESENT);
		out.subject=entity.getSubject();
		out.isGeneric=(entity.getGeneric() == CONST.NE_GENERIC_TRUE);
		out.isConditional=(entity.getConditional() == CONST.NE_CONDITIONAL_TRUE );
		out.isHistorical=(entity.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT );
		return out;
	}
	
	public static class Unspecified {

		public String cui;
		public String type;
		public boolean isHistorical;
		public boolean isConditional;
		public boolean isGeneric;
		public String subject;
		public boolean isUncertain;
		public int polarity;
		public String coveredText;
		public int begin;
		public int end;
		
		public boolean isHistorical() {
			return isHistorical;
		}
		public boolean isConditional() {
			return isConditional;
		}
		public boolean isGeneric() {
			return isGeneric;
		}
		public String getSubject() {
			return subject;
		}
		public boolean isUncertain() {
			return isUncertain;
		}
		public int getPolarity() {
			return polarity;
		}
		public String getCoveredText() {
			return coveredText;
		}
		public int getBegin() {
			return begin;
		}
		public int getEnd() {
			return end;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getCui() {
			return cui;
		}
		public void setCui(String cui) {
			this.cui = cui;
		}
		
	}
	
	public static class Type {
		static final int OTHER = 0;
		static final int PROCEDURE = 1;
		static final int OBSERVATION = 2;
		static final int MEDICATION = 3;
		static final int LAB_TEST = 4;
		static final int DIAGNOSIS = 5;
	}
	
	public static class Flag {
		static final int TEMPORAL_CONTEXT_PRESENT = 64;
		static final int TEMPORAL_CONTEXT_PAST = 64*2;
		static final int KNOWN_PRESENT = 64*4;
		static final int KNOWN_ABSENT = 64*8;
		static final int SUBJECT_OF_RECORD_PATIENT = 64*16;
	}

	public static <X extends IdentifiedAnnotation> Integer codeFor(X jcas) {
		int out;
		if (jcas instanceof SignSymptomMention)
			out = Type.OBSERVATION;
		else if (jcas instanceof DiseaseDisorderMention)
			out = Type.OBSERVATION;
		else if (jcas instanceof MedicationMention)
			out = Type.MEDICATION;
		else if (jcas instanceof ProcedureMention)
			out = Type.PROCEDURE;
		else
			out = Type.OTHER;
		
		if (jcas.getPolarity() == CONST.NE_POLARITY_NEGATION_ABSENT) out += Flag.KNOWN_PRESENT;
		if (jcas.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT) out += Flag.KNOWN_ABSENT;
		if (jcas.getSubject().equals(CONST.ATTR_SUBJECT_PATIENT)) out += Flag.SUBJECT_OF_RECORD_PATIENT;
		if (jcas.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT ) out += Flag.TEMPORAL_CONTEXT_PAST;
		if (jcas.getHistoryOf() == CONST.NE_HISTORY_OF_ABSENT ) out += Flag.TEMPORAL_CONTEXT_PRESENT;
		return out;
	}
}
