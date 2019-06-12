package uk.co.terminological.ctakes;

//import org.apache.ctakes.typesystem.type.syntax.WordToken;
//import org.apache.ctakes.typesystem.type.syntax.ConllDependencyNode;
import org.apache.ctakes.typesystem.type.textsem.MeasurementAnnotation;
import org.apache.ctakes.typesystem.type.textsem.MedicationEventMention;
import org.apache.ctakes.typesystem.type.textsem.RangeAnnotation;
import org.apache.ctakes.typesystem.type.textsem.RomanNumeralAnnotation;
import org.apache.ctakes.typesystem.type.textsem.DateAnnotation;
import org.apache.ctakes.typesystem.type.textsem.FractionAnnotation;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.ctakes.typesystem.type.textsem.LabMention;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.ctakes.typesystem.type.textsem.AnatomicalSiteMention;
import org.apache.ctakes.typesystem.type.textsem.MedicationMention;
import org.apache.ctakes.typesystem.type.textsem.ProcedureMention;
import org.apache.ctakes.typesystem.type.textsem.SignSymptomMention;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.ctakes.typesystem.type.textsem.DiseaseDisorderMention;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;

import uk.co.terminological.omop.*;

public class JcasOmopMapper {

	private static final Integer PRESENT = 4181412; // from SNOMED
	private static final Integer ABSENT = 4132135; // from SNOMED
	private static final Integer CONDITION_TYPE_NLP_DERIVED=32424;
	private static final Integer MEAS_TYPE_NLP_DERIVED=32423;
	private static final Integer PROCEDURE_TYPE_NLP_DERIVED=32425;
	private static final Integer DRUG_TYPE_NLP_DERIVED=32426;
	private static final  Integer OBSERVATION_TYPE_NLP_DERIVED=32445;
	
	
	private static final  Integer CONDITION_STATUS_FINAL_DIAGNOSIS=4230359;
	private static final  Integer SECTION_CONCEPT_CLINICAL_DOCUMENT=4309829; //concept_class_id = 'Record Artifact'
	
	//public static WordVector map(WordToken token) {}
	Map<String,CuiOmopMap> conceptMapper;
	CuiOmopMap defaultMap = Factory.Mutable.createCuiOmopMap().withCode("Unknown").withConceptId(0);
	Gson gson = new GsonBuilder().create();
	
	public JcasOmopMapper(Database db) throws SQLException {
		conceptMapper = new HashMap<>();
		db.read().streamCuiOmopMap().forEach(
			com -> conceptMapper.put(com.getCui(),com)
		);
	}
	
	public CuiOmopMap mapConcept(IdentifiedAnnotation jcas) {
		if (jcas == null || jcas.getOntologyConceptArr() == null) return defaultMap;
		return Stream.of(jcas.getOntologyConceptArr().toArray())
			.filter(fs -> fs instanceof UmlsConcept)
			.findFirst()
			.map(u -> ((UmlsConcept) u).getCui())
			.map(cui -> conceptMapper.getOrDefault(cui, defaultMap))
			.orElse(defaultMap);
	}
	
	public boolean isValidAndCurrent(IdentifiedAnnotation jcas) {
		return 
				jcas.getPolarity() == 1 &&
				jcas.getSubject().equals(CONST.ATTR_SUBJECT_PATIENT) && 
				jcas.getHistoryOf() == CONST.NE_HISTORY_OF_ABSENT;
	}
	
	public <X extends IdentifiedAnnotation> NoteNlp mapNote(UnprocessedNote note, X jcas) {
		Integer conceptId = mapConcept(jcas).getConceptId();
		NoteNlpFluent noteNlp = Factory.Mutable.createNoteNlp()
				.withNoteNlpConceptId(conceptId)
				.withNoteNlpSourceConceptId(conceptId)
				.withSnippet(note.getNoteTitle())
				.withLexicalVariant(jcas.getCoveredText())
				.withNlpDate(Date.valueOf(LocalDate.now()))
				.withNlpDatetime(Timestamp.valueOf(LocalDateTime.now()))
				.withNlpSystem("CTAKES") //TODO: versioning
				.withNoteId(note.getNoteId())
				// .withOffset(null) ?unclear in spec
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
						) ?"historical":"current");
		
		Object omopEntry;
		if (!isValidAndCurrent(jcas))
			omopEntry = this.map(note, jcas); //Default to just a note_nlp entry 
		else {
			if (jcas instanceof SignSymptomMention)
				omopEntry = this.map(note, (SignSymptomMention) jcas);
			else if (jcas instanceof DiseaseDisorderMention)
				omopEntry = this.map(note, (DiseaseDisorderMention) jcas);
			else if (jcas instanceof MedicationMention)
				omopEntry = this.map(note, (MedicationMention) jcas);
			else if (jcas instanceof ProcedureMention)
				omopEntry = this.map(note, (ProcedureMention) jcas);
			else
				omopEntry = this.map(note, jcas);
		}
			
		noteNlp.withTermModifier(gson.toJson(omopEntry));
		return noteNlp;
	}
	
	public ConditionOccurrence map(UnprocessedNote note, DiseaseDisorderMention jcas) {
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
	}
	
	public DrugExposure map(UnprocessedNote note, MedicationMention jcas) {
		Integer conceptId = mapConcept(jcas).getConceptId();
		DrugExposure out = Factory.Mutable.createDrugExposure()
				.withDrugConceptId(conceptId)
				.withPersonId(note.getPersonId())
				.withDrugExposureStartDate(note.getNoteDate())
				.withDrugExposureStartDatetime(note.getNoteDatetime())
				.withDrugExposureEndDatetime(note.getNoteDatetime())
				.withRouteConceptId(0) //unknown
				.withDrugSourceConceptId(conceptId)
				.withDrugSourceValue(jcas.getCoveredText());
		return out;
	}
	
	/*public DrugExposure map(UnprocessedNote note, MedicationEventMention jcas) {
		throw new NotImplementedException();
	}*/
	
	public Observation map(UnprocessedNote note, SignSymptomMention jcas) {
		Integer conceptId = mapConcept(jcas).getConceptId();
		Observation out = Factory.Mutable.createObservation()
				.withObservationConceptId(conceptId)
				.withPersonId(note.getPersonId())
				.withObservationDate(note.getNoteDate())
				.withObservationDatetime(note.getNoteDatetime())
				.withObservationSourceValue(jcas.getCoveredText())
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
	
	public ProcedureOccurrence map(UnprocessedNote note, ProcedureMention jcas) {
		Integer conceptId = mapConcept(jcas).getConceptId();
		ProcedureOccurrence out = Factory.Mutable.createProcedureOccurrence()
				.withPersonId(note.getPersonId())
				.withProcedureDate(note.getNoteDate())
				.withProcedureDatetime(note.getNoteDatetime())
				.withProcedureSourceValue(jcas.getCoveredText())
				.withProcedureSourceConceptId(conceptId)
				.withProcedureTypeConceptId(PROCEDURE_TYPE_NLP_DERIVED)
				.withModifierConceptId(mapConcept(jcas.getBodyLaterality()).getConceptId()); //defaults to 0
		return out;
	}
	
	/*public Measurement map(UnprocessedNote note, LabMention jcas) {
		throw new NotImplementedException();
	}*/
	
	public Unspecified map(UnprocessedNote note, IdentifiedAnnotation entity) {
		Unspecified out = new Unspecified();
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
		
	}
}
