package uk.co.terminological.omop;

import javax.annotation.Generated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;
import java.util.function.Consumer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import uk.co.terminological.omop.DrugExposure;
import uk.co.terminological.omop.DrugExposureSql;
import uk.co.terminological.omop.NoteNlp;
import uk.co.terminological.omop.NoteNlpSql;
import uk.co.terminological.omop.ProcedureOccurrence;
import uk.co.terminological.omop.ProcedureOccurrenceSql;
import uk.co.terminological.omop.NlpAudit;
import uk.co.terminological.omop.NlpAuditSql;
import uk.co.terminological.omop.ConditionOccurrence;
import uk.co.terminological.omop.ConditionOccurrenceSql;
import uk.co.terminological.omop.Observation;
import uk.co.terminological.omop.ObservationSql;
import uk.co.terminological.omop.Input;
import uk.co.terminological.omop.InputSql;
import uk.co.terminological.omop.Measurement;
import uk.co.terminological.omop.MeasurementSql;
import uk.co.terminological.omop.Note;
import uk.co.terminological.omop.NoteSql;
import uk.co.terminological.omop.CuiOmopMap;
import uk.co.terminological.omop.CuiOmopMapSql;

/**
*	The SqlFactory
*
*	@author: javapig. oink, oink! 
*/
@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class Database {

	//Coordination and indexing of the output
	
	Connection conn;
	
	public static Database from(Path config) {
		
			Properties prop =  new Properties();
			try {
				prop.load(Files.newInputStream(config));
			} catch (IOException e) {
				throw new RuntimeException("exception loading properties file: "+e.getLocalizedMessage(), e);
			}
			
			return new Database(prop);
			
	}
	
	public Database(Properties prop) {
		try {
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop);
		} catch (Exception e) {
			throw new RuntimeException("exception setting up database connection: "+e.getLocalizedMessage(), e);
		}
	}

	/*************** RESULT SET UTILITY FUNCTIONS **************************/
	
	private static interface FunctionWithException<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
	
	private static <X, E extends Exception> Stream<X> streamResultSet(ResultSet rs, FunctionWithException<ResultSet,X, E> mapper) {
		Iterable<X> iterable = () -> iterateResultSet(rs,mapper);
		return StreamSupport.stream(iterable.spliterator(), false).onClose(() -> {
			try {
				rs.close();
			} catch (SQLException e) {
				//we tried;
			}
		});
	}
	
	private static <X, E extends Exception> Iterator<X> iterateResultSet(ResultSet rs, FunctionWithException<ResultSet,X,E> mapper) {
		return new Iterator<X>() {

			X out = null;
			@Override
			public boolean hasNext() {
				try {
					if (out == null) {
						boolean ready = rs.next();
						if (ready) out = mapper.apply(rs);
					}
				} catch (Exception e) {
					out = null;
				} 
				return out != null;
			}

			@Override
			public X next() {
				if (!hasNext()) throw new NoSuchElementException();
				X tmp = out;
				out = null;
				return tmp;
			}
			
		};
	}
	
	private static Map<String,Object> rowToMap(ResultSet rs) throws SQLException {
		Map<String, Object> out = new HashMap<>();
		ResultSetMetaData rsm = rs.getMetaData();
		for (int i=0; i<rsm.getColumnCount(); i++) {
			out.put(
				rsm.getColumnName(i+1), 
				rs.getObject(i+1)
			);
		}
		return out;
	}
	
	/*************** GENERAL DB FUNCTIONS **************************/
	
	public int apply(String preparedSql, Object... parameters) throws SQLException {
		int i = 1;
		PreparedStatement pst = conn.prepareStatement(preparedSql);
		for (Object parameter: parameters) {
			pst.setObject(i, parameter);
		}
    	return pst.executeUpdate();
	}
	
	public Stream<Map<String,Object>> retrieve(String preparedSql, Object... parameters) throws SQLException {
		int i = 1;
		PreparedStatement pst = conn.prepareStatement(preparedSql);
		for (Object parameter: parameters) {
			pst.setObject(i, parameter);
		}
    	ResultSet rs = pst.executeQuery();
    	return streamResultSet(rs,r -> rowToMap(r));
	}
	
	/*************** TABLE READERS **************************/
	
	public Reader read() throws SQLException {
		if (reader == null) reader = new Reader(conn);
		return reader;
	}
	
	Reader reader;
	
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Reader {
	
		PreparedStatement pstDrugExposure;
		PreparedStatement pstNoteNlp;
		PreparedStatement pstProcedureOccurrence;
		PreparedStatement pstNlpAudit;
		PreparedStatement pstConditionOccurrence;
		PreparedStatement pstObservation;
		PreparedStatement pstMeasurement;
		PreparedStatement pstNote;
		PreparedStatement pstCuiOmopMap;

		Reader(Connection conn) throws SQLException {
			pstDrugExposure = conn.prepareStatement("select * from omop.dbo.drug_exposure");
			pstNoteNlp = conn.prepareStatement("select * from omop.dbo.note_nlp");
			pstProcedureOccurrence = conn.prepareStatement("select * from omop.dbo.procedure_occurrence");
			pstNlpAudit = conn.prepareStatement("select * from omopBuild.dbo.NlpAudit");
			pstConditionOccurrence = conn.prepareStatement("select * from omop.dbo.condition_occurrence");
			pstObservation = conn.prepareStatement("select * from omop.dbo.observation");
			pstMeasurement = conn.prepareStatement("select * from omop.dbo.measurement");
			pstNote = conn.prepareStatement("select * from omop.dbo.note");
			pstCuiOmopMap = conn.prepareStatement("select * from omopBuild.dbo.CuiOmopMap");
		}
	
	
		public Iterator<DrugExposure> fromDrugExposure() throws SQLException {
			return fromDrugExposure(0);
		}

		public Iterator<DrugExposure> fromDrugExposure(int limit) throws SQLException {
			if (limit >= 0) pstDrugExposure.setMaxRows(limit);
    		ResultSet rs = pstDrugExposure.executeQuery();
    		return iterateResultSet(rs,r -> new DrugExposureSql(rs));
    	}
	
	
		public Stream<DrugExposure> streamDrugExposure() throws SQLException {
			return streamDrugExposure(0);
		}
		
		public Stream<DrugExposure> streamDrugExposure(int limit) throws SQLException {
			if (limit >= 0) pstDrugExposure.setMaxRows(limit);
    		ResultSet rs = pstDrugExposure.executeQuery();
    		return streamResultSet(rs,r -> new DrugExposureSql(rs));
		}
	
		public Iterator<NoteNlp> fromNoteNlp() throws SQLException {
			return fromNoteNlp(0);
		}

		public Iterator<NoteNlp> fromNoteNlp(int limit) throws SQLException {
			if (limit >= 0) pstNoteNlp.setMaxRows(limit);
    		ResultSet rs = pstNoteNlp.executeQuery();
    		return iterateResultSet(rs,r -> new NoteNlpSql(rs));
    	}
	
	
		public Stream<NoteNlp> streamNoteNlp() throws SQLException {
			return streamNoteNlp(0);
		}
		
		public Stream<NoteNlp> streamNoteNlp(int limit) throws SQLException {
			if (limit >= 0) pstNoteNlp.setMaxRows(limit);
    		ResultSet rs = pstNoteNlp.executeQuery();
    		return streamResultSet(rs,r -> new NoteNlpSql(rs));
		}
	
		public Iterator<ProcedureOccurrence> fromProcedureOccurrence() throws SQLException {
			return fromProcedureOccurrence(0);
		}

		public Iterator<ProcedureOccurrence> fromProcedureOccurrence(int limit) throws SQLException {
			if (limit >= 0) pstProcedureOccurrence.setMaxRows(limit);
    		ResultSet rs = pstProcedureOccurrence.executeQuery();
    		return iterateResultSet(rs,r -> new ProcedureOccurrenceSql(rs));
    	}
	
	
		public Stream<ProcedureOccurrence> streamProcedureOccurrence() throws SQLException {
			return streamProcedureOccurrence(0);
		}
		
		public Stream<ProcedureOccurrence> streamProcedureOccurrence(int limit) throws SQLException {
			if (limit >= 0) pstProcedureOccurrence.setMaxRows(limit);
    		ResultSet rs = pstProcedureOccurrence.executeQuery();
    		return streamResultSet(rs,r -> new ProcedureOccurrenceSql(rs));
		}
	
		public Iterator<NlpAudit> fromNlpAudit() throws SQLException {
			return fromNlpAudit(0);
		}

		public Iterator<NlpAudit> fromNlpAudit(int limit) throws SQLException {
			if (limit >= 0) pstNlpAudit.setMaxRows(limit);
    		ResultSet rs = pstNlpAudit.executeQuery();
    		return iterateResultSet(rs,r -> new NlpAuditSql(rs));
    	}
	
	
		public Stream<NlpAudit> streamNlpAudit() throws SQLException {
			return streamNlpAudit(0);
		}
		
		public Stream<NlpAudit> streamNlpAudit(int limit) throws SQLException {
			if (limit >= 0) pstNlpAudit.setMaxRows(limit);
    		ResultSet rs = pstNlpAudit.executeQuery();
    		return streamResultSet(rs,r -> new NlpAuditSql(rs));
		}
	
		public Iterator<ConditionOccurrence> fromConditionOccurrence() throws SQLException {
			return fromConditionOccurrence(0);
		}

		public Iterator<ConditionOccurrence> fromConditionOccurrence(int limit) throws SQLException {
			if (limit >= 0) pstConditionOccurrence.setMaxRows(limit);
    		ResultSet rs = pstConditionOccurrence.executeQuery();
    		return iterateResultSet(rs,r -> new ConditionOccurrenceSql(rs));
    	}
	
	
		public Stream<ConditionOccurrence> streamConditionOccurrence() throws SQLException {
			return streamConditionOccurrence(0);
		}
		
		public Stream<ConditionOccurrence> streamConditionOccurrence(int limit) throws SQLException {
			if (limit >= 0) pstConditionOccurrence.setMaxRows(limit);
    		ResultSet rs = pstConditionOccurrence.executeQuery();
    		return streamResultSet(rs,r -> new ConditionOccurrenceSql(rs));
		}
	
		public Iterator<Observation> fromObservation() throws SQLException {
			return fromObservation(0);
		}

		public Iterator<Observation> fromObservation(int limit) throws SQLException {
			if (limit >= 0) pstObservation.setMaxRows(limit);
    		ResultSet rs = pstObservation.executeQuery();
    		return iterateResultSet(rs,r -> new ObservationSql(rs));
    	}
	
	
		public Stream<Observation> streamObservation() throws SQLException {
			return streamObservation(0);
		}
		
		public Stream<Observation> streamObservation(int limit) throws SQLException {
			if (limit >= 0) pstObservation.setMaxRows(limit);
    		ResultSet rs = pstObservation.executeQuery();
    		return streamResultSet(rs,r -> new ObservationSql(rs));
		}
	
		public Iterator<Measurement> fromMeasurement() throws SQLException {
			return fromMeasurement(0);
		}

		public Iterator<Measurement> fromMeasurement(int limit) throws SQLException {
			if (limit >= 0) pstMeasurement.setMaxRows(limit);
    		ResultSet rs = pstMeasurement.executeQuery();
    		return iterateResultSet(rs,r -> new MeasurementSql(rs));
    	}
	
	
		public Stream<Measurement> streamMeasurement() throws SQLException {
			return streamMeasurement(0);
		}
		
		public Stream<Measurement> streamMeasurement(int limit) throws SQLException {
			if (limit >= 0) pstMeasurement.setMaxRows(limit);
    		ResultSet rs = pstMeasurement.executeQuery();
    		return streamResultSet(rs,r -> new MeasurementSql(rs));
		}
	
		public Iterator<Note> fromNote() throws SQLException {
			return fromNote(0);
		}

		public Iterator<Note> fromNote(int limit) throws SQLException {
			if (limit >= 0) pstNote.setMaxRows(limit);
    		ResultSet rs = pstNote.executeQuery();
    		return iterateResultSet(rs,r -> new NoteSql(rs));
    	}
	
	
		public Stream<Note> streamNote() throws SQLException {
			return streamNote(0);
		}
		
		public Stream<Note> streamNote(int limit) throws SQLException {
			if (limit >= 0) pstNote.setMaxRows(limit);
    		ResultSet rs = pstNote.executeQuery();
    		return streamResultSet(rs,r -> new NoteSql(rs));
		}
	
		public Iterator<CuiOmopMap> fromCuiOmopMap() throws SQLException {
			return fromCuiOmopMap(0);
		}

		public Iterator<CuiOmopMap> fromCuiOmopMap(int limit) throws SQLException {
			if (limit >= 0) pstCuiOmopMap.setMaxRows(limit);
    		ResultSet rs = pstCuiOmopMap.executeQuery();
    		return iterateResultSet(rs,r -> new CuiOmopMapSql(rs));
    	}
	
	
		public Stream<CuiOmopMap> streamCuiOmopMap() throws SQLException {
			return streamCuiOmopMap(0);
		}
		
		public Stream<CuiOmopMap> streamCuiOmopMap(int limit) throws SQLException {
			if (limit >= 0) pstCuiOmopMap.setMaxRows(limit);
    		ResultSet rs = pstCuiOmopMap.executeQuery();
    		return streamResultSet(rs,r -> new CuiOmopMapSql(rs));
		}
	}
	
	/*************** TABLE WRITERS **************************/

	public Writer write() throws SQLException {
		if (writer == null) writer = new Writer(conn);
		return writer;
	}
	
	public Writer writer;
	
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Writer{
	
		PreparedStatement pstDrugExposure;
		PreparedStatement pstNoteNlp;
		PreparedStatement pstProcedureOccurrence;
		PreparedStatement pstNlpAudit;
		PreparedStatement pstConditionOccurrence;
		PreparedStatement pstObservation;
		PreparedStatement pstMeasurement;
		PreparedStatement pstNote;
		PreparedStatement pstCuiOmopMap;

		Writer(Connection conn) throws SQLException {
			pstDrugExposure = conn.prepareStatement("insert into omop.dbo.drug_exposure "+
				"(drug_exposure_id,person_id,drug_concept_id,drug_exposure_start_date,drug_exposure_start_datetime,drug_exposure_end_date,drug_exposure_end_datetime,verbatim_end_date,drug_type_concept_id,stop_reason,refills,quantity,days_supply,sig,route_concept_id,lot_number,provider_id,visit_occurrence_id,visit_detail_id,drug_source_value,drug_source_concept_id,route_source_value,dose_unit_source_value)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstNoteNlp = conn.prepareStatement("insert into omop.dbo.note_nlp "+
				"(note_id,section_concept_id,snippet,offset,lexical_variant,note_nlp_concept_id,nlp_system,nlp_date,nlp_datetime,term_exists,term_temporal,term_modifiers,note_nlp_source_concept_id,custom_code)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstProcedureOccurrence = conn.prepareStatement("insert into omop.dbo.procedure_occurrence "+
				"(procedure_occurrence_id,person_id,procedure_concept_id,procedure_date,procedure_datetime,procedure_type_concept_id,modifier_concept_id,quantity,provider_id,visit_occurrence_id,visit_detail_id,procedure_source_value,procedure_source_concept_id,modifier_source_value)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstNlpAudit = conn.prepareStatement("insert into omopBuild.dbo.NlpAudit "+
				"(note_id,event_time,nlp_system,nlp_system_instance,event_type,event_detail)"+
				" values (?,?,?,?,?,?)"
			);
			pstConditionOccurrence = conn.prepareStatement("insert into omop.dbo.condition_occurrence "+
				"(condition_occurrence_id,person_id,condition_concept_id,condition_start_date,condition_start_datetime,condition_end_date,condition_end_datetime,condition_type_concept_id,condition_status_concept_id,stop_reason,provider_id,visit_occurrence_id,visit_detail_id,condition_source_value,condition_source_concept_id,condition_status_source_value)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstObservation = conn.prepareStatement("insert into omop.dbo.observation "+
				"(observation_id,person_id,observation_concept_id,observation_date,observation_datetime,observation_type_concept_id,value_as_number,value_as_string,value_as_concept_id,qualifier_concept_id,unit_concept_id,provider_id,visit_occurrence_id,visit_detail_id,observation_source_value,observation_source_concept_id,unit_source_value,qualifier_source_value,observation_event_id,obs_event_field_concept_id,value_as_datetime)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstMeasurement = conn.prepareStatement("insert into omop.dbo.measurement "+
				"(measurement_id,person_id,measurement_concept_id,measurement_date,measurement_datetime,measurement_time,measurement_type_concept_id,operator_concept_id,value_as_number,value_as_concept_id,unit_concept_id,range_low,range_high,provider_id,visit_occurrence_id,visit_detail_id,measurement_source_value,measurement_source_concept_id,unit_source_value,value_source_value)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstNote = conn.prepareStatement("insert into omop.dbo.note "+
				"(note_id,person_id,note_event_id,note_event_field_concept_id,note_date,note_datetime,note_type_concept_id,note_class_concept_id,note_title,note_text,encoding_concept_id,language_concept_id,provider_id,visit_occurrence_id,visit_detail_id,note_source_value)"+
				" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
			pstCuiOmopMap = conn.prepareStatement("insert into omopBuild.dbo.CuiOmopMap "+
				"(CUI,source_concept_id,concept_id)"+
				" values (?,?,?)"
			);
		}

		public Consumer<DrugExposure> ofDrugExposure(final boolean rethrow) {
    		return new Consumer<DrugExposure>() {
    			@Override
    			public void accept(DrugExposure input) {
    				try {
    					writeDrugExposure(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeDrugExposure(DrugExposure input) throws SQLException {
			pstDrugExposure.clearParameters();
    		pstDrugExposure.setObject(1, input.getDrugExposureId());
    		pstDrugExposure.setObject(2, input.getPersonId());
    		pstDrugExposure.setObject(3, input.getDrugConceptId());
    		pstDrugExposure.setObject(4, input.getDrugExposureStartDate());
    		pstDrugExposure.setObject(5, input.getDrugExposureStartDatetime());
    		pstDrugExposure.setObject(6, input.getDrugExposureEndDate());
    		pstDrugExposure.setObject(7, input.getDrugExposureEndDatetime());
    		pstDrugExposure.setObject(8, input.getVerbatimEndDate());
    		pstDrugExposure.setObject(9, input.getDrugTypeConceptId());
    		pstDrugExposure.setObject(10, input.getStopReason());
    		pstDrugExposure.setObject(11, input.getRefills());
    		pstDrugExposure.setObject(12, input.getQuantity());
    		pstDrugExposure.setObject(13, input.getDaysSupply());
    		pstDrugExposure.setObject(14, input.getSig());
    		pstDrugExposure.setObject(15, input.getRouteConceptId());
    		pstDrugExposure.setObject(16, input.getLotNumber());
    		pstDrugExposure.setObject(17, input.getProviderId());
    		pstDrugExposure.setObject(18, input.getVisitOccurrenceId());
    		pstDrugExposure.setObject(19, input.getVisitDetailId());
    		pstDrugExposure.setObject(20, input.getDrugSourceValue());
    		pstDrugExposure.setObject(21, input.getDrugSourceConceptId());
    		pstDrugExposure.setObject(22, input.getRouteSourceValue());
    		pstDrugExposure.setObject(23, input.getDoseUnitSourceValue());
    		return pstDrugExposure.executeUpdate();
    	}
	
		public int writeBatchDrugExposure(Collection<DrugExposure> inputs) throws SQLException {
			return writeBatchDrugExposure(inputs,0);
		}
	
		public int writeBatchDrugExposure(Collection<DrugExposure> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (DrugExposure input: inputs) {
				pstDrugExposure.clearParameters();
    			pstDrugExposure.setObject(1, input.getDrugExposureId());
    			pstDrugExposure.setObject(2, input.getPersonId());
    			pstDrugExposure.setObject(3, input.getDrugConceptId());
    			pstDrugExposure.setObject(4, input.getDrugExposureStartDate());
    			pstDrugExposure.setObject(5, input.getDrugExposureStartDatetime());
    			pstDrugExposure.setObject(6, input.getDrugExposureEndDate());
    			pstDrugExposure.setObject(7, input.getDrugExposureEndDatetime());
    			pstDrugExposure.setObject(8, input.getVerbatimEndDate());
    			pstDrugExposure.setObject(9, input.getDrugTypeConceptId());
    			pstDrugExposure.setObject(10, input.getStopReason());
    			pstDrugExposure.setObject(11, input.getRefills());
    			pstDrugExposure.setObject(12, input.getQuantity());
    			pstDrugExposure.setObject(13, input.getDaysSupply());
    			pstDrugExposure.setObject(14, input.getSig());
    			pstDrugExposure.setObject(15, input.getRouteConceptId());
    			pstDrugExposure.setObject(16, input.getLotNumber());
    			pstDrugExposure.setObject(17, input.getProviderId());
    			pstDrugExposure.setObject(18, input.getVisitOccurrenceId());
    			pstDrugExposure.setObject(19, input.getVisitDetailId());
    			pstDrugExposure.setObject(20, input.getDrugSourceValue());
    			pstDrugExposure.setObject(21, input.getDrugSourceConceptId());
    			pstDrugExposure.setObject(22, input.getRouteSourceValue());
    			pstDrugExposure.setObject(23, input.getDoseUnitSourceValue());
    			pstDrugExposure.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstDrugExposure.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstDrugExposure.executeBatch()).sum();
    		return affected;
		}
		public Consumer<NoteNlp> ofNoteNlp(final boolean rethrow) {
    		return new Consumer<NoteNlp>() {
    			@Override
    			public void accept(NoteNlp input) {
    				try {
    					writeNoteNlp(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeNoteNlp(NoteNlp input) throws SQLException {
			pstNoteNlp.clearParameters();
    		pstNoteNlp.setObject(1, input.getNoteId());
    		pstNoteNlp.setObject(2, input.getSectionConceptId());
    		pstNoteNlp.setObject(3, input.getSnippet());
    		pstNoteNlp.setObject(4, input.getOffset());
    		pstNoteNlp.setObject(5, input.getLexicalVariant());
    		pstNoteNlp.setObject(6, input.getNoteNlpConceptId());
    		pstNoteNlp.setObject(7, input.getNlpSystem());
    		pstNoteNlp.setObject(8, input.getNlpDate());
    		pstNoteNlp.setObject(9, input.getNlpDatetime());
    		pstNoteNlp.setObject(10, input.getTermExists());
    		pstNoteNlp.setObject(11, input.getTermTemporal());
    		pstNoteNlp.setObject(12, input.getTermModifiers());
    		pstNoteNlp.setObject(13, input.getNoteNlpSourceConceptId());
    		pstNoteNlp.setObject(14, input.getCustomCode());
    		return pstNoteNlp.executeUpdate();
    	}
	
		public int writeBatchNoteNlp(Collection<NoteNlp> inputs) throws SQLException {
			return writeBatchNoteNlp(inputs,0);
		}
	
		public int writeBatchNoteNlp(Collection<NoteNlp> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (NoteNlp input: inputs) {
				pstNoteNlp.clearParameters();
    			pstNoteNlp.setObject(1, input.getNoteId());
    			pstNoteNlp.setObject(2, input.getSectionConceptId());
    			pstNoteNlp.setObject(3, input.getSnippet());
    			pstNoteNlp.setObject(4, input.getOffset());
    			pstNoteNlp.setObject(5, input.getLexicalVariant());
    			pstNoteNlp.setObject(6, input.getNoteNlpConceptId());
    			pstNoteNlp.setObject(7, input.getNlpSystem());
    			pstNoteNlp.setObject(8, input.getNlpDate());
    			pstNoteNlp.setObject(9, input.getNlpDatetime());
    			pstNoteNlp.setObject(10, input.getTermExists());
    			pstNoteNlp.setObject(11, input.getTermTemporal());
    			pstNoteNlp.setObject(12, input.getTermModifiers());
    			pstNoteNlp.setObject(13, input.getNoteNlpSourceConceptId());
    			pstNoteNlp.setObject(14, input.getCustomCode());
    			pstNoteNlp.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstNoteNlp.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstNoteNlp.executeBatch()).sum();
    		return affected;
		}
		public Consumer<ProcedureOccurrence> ofProcedureOccurrence(final boolean rethrow) {
    		return new Consumer<ProcedureOccurrence>() {
    			@Override
    			public void accept(ProcedureOccurrence input) {
    				try {
    					writeProcedureOccurrence(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeProcedureOccurrence(ProcedureOccurrence input) throws SQLException {
			pstProcedureOccurrence.clearParameters();
    		pstProcedureOccurrence.setObject(1, input.getProcedureOccurrenceId());
    		pstProcedureOccurrence.setObject(2, input.getPersonId());
    		pstProcedureOccurrence.setObject(3, input.getProcedureConceptId());
    		pstProcedureOccurrence.setObject(4, input.getProcedureDate());
    		pstProcedureOccurrence.setObject(5, input.getProcedureDatetime());
    		pstProcedureOccurrence.setObject(6, input.getProcedureTypeConceptId());
    		pstProcedureOccurrence.setObject(7, input.getModifierConceptId());
    		pstProcedureOccurrence.setObject(8, input.getQuantity());
    		pstProcedureOccurrence.setObject(9, input.getProviderId());
    		pstProcedureOccurrence.setObject(10, input.getVisitOccurrenceId());
    		pstProcedureOccurrence.setObject(11, input.getVisitDetailId());
    		pstProcedureOccurrence.setObject(12, input.getProcedureSourceValue());
    		pstProcedureOccurrence.setObject(13, input.getProcedureSourceConceptId());
    		pstProcedureOccurrence.setObject(14, input.getModifierSourceValue());
    		return pstProcedureOccurrence.executeUpdate();
    	}
	
		public int writeBatchProcedureOccurrence(Collection<ProcedureOccurrence> inputs) throws SQLException {
			return writeBatchProcedureOccurrence(inputs,0);
		}
	
		public int writeBatchProcedureOccurrence(Collection<ProcedureOccurrence> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (ProcedureOccurrence input: inputs) {
				pstProcedureOccurrence.clearParameters();
    			pstProcedureOccurrence.setObject(1, input.getProcedureOccurrenceId());
    			pstProcedureOccurrence.setObject(2, input.getPersonId());
    			pstProcedureOccurrence.setObject(3, input.getProcedureConceptId());
    			pstProcedureOccurrence.setObject(4, input.getProcedureDate());
    			pstProcedureOccurrence.setObject(5, input.getProcedureDatetime());
    			pstProcedureOccurrence.setObject(6, input.getProcedureTypeConceptId());
    			pstProcedureOccurrence.setObject(7, input.getModifierConceptId());
    			pstProcedureOccurrence.setObject(8, input.getQuantity());
    			pstProcedureOccurrence.setObject(9, input.getProviderId());
    			pstProcedureOccurrence.setObject(10, input.getVisitOccurrenceId());
    			pstProcedureOccurrence.setObject(11, input.getVisitDetailId());
    			pstProcedureOccurrence.setObject(12, input.getProcedureSourceValue());
    			pstProcedureOccurrence.setObject(13, input.getProcedureSourceConceptId());
    			pstProcedureOccurrence.setObject(14, input.getModifierSourceValue());
    			pstProcedureOccurrence.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstProcedureOccurrence.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstProcedureOccurrence.executeBatch()).sum();
    		return affected;
		}
		public Consumer<NlpAudit> ofNlpAudit(final boolean rethrow) {
    		return new Consumer<NlpAudit>() {
    			@Override
    			public void accept(NlpAudit input) {
    				try {
    					writeNlpAudit(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeNlpAudit(NlpAudit input) throws SQLException {
			pstNlpAudit.clearParameters();
    		pstNlpAudit.setObject(1, input.getNoteId());
    		pstNlpAudit.setObject(2, input.getEventTime());
    		pstNlpAudit.setObject(3, input.getNlpSystem());
    		pstNlpAudit.setObject(4, input.getNlpSystemInstance());
    		pstNlpAudit.setObject(5, input.getEventType());
    		pstNlpAudit.setObject(6, input.getEventDetail());
    		return pstNlpAudit.executeUpdate();
    	}
	
		public int writeBatchNlpAudit(Collection<NlpAudit> inputs) throws SQLException {
			return writeBatchNlpAudit(inputs,0);
		}
	
		public int writeBatchNlpAudit(Collection<NlpAudit> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (NlpAudit input: inputs) {
				pstNlpAudit.clearParameters();
    			pstNlpAudit.setObject(1, input.getNoteId());
    			pstNlpAudit.setObject(2, input.getEventTime());
    			pstNlpAudit.setObject(3, input.getNlpSystem());
    			pstNlpAudit.setObject(4, input.getNlpSystemInstance());
    			pstNlpAudit.setObject(5, input.getEventType());
    			pstNlpAudit.setObject(6, input.getEventDetail());
    			pstNlpAudit.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstNlpAudit.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstNlpAudit.executeBatch()).sum();
    		return affected;
		}
		public Consumer<ConditionOccurrence> ofConditionOccurrence(final boolean rethrow) {
    		return new Consumer<ConditionOccurrence>() {
    			@Override
    			public void accept(ConditionOccurrence input) {
    				try {
    					writeConditionOccurrence(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeConditionOccurrence(ConditionOccurrence input) throws SQLException {
			pstConditionOccurrence.clearParameters();
    		pstConditionOccurrence.setObject(1, input.getConditionOccurrenceId());
    		pstConditionOccurrence.setObject(2, input.getPersonId());
    		pstConditionOccurrence.setObject(3, input.getConditionConceptId());
    		pstConditionOccurrence.setObject(4, input.getConditionStartDate());
    		pstConditionOccurrence.setObject(5, input.getConditionStartDatetime());
    		pstConditionOccurrence.setObject(6, input.getConditionEndDate());
    		pstConditionOccurrence.setObject(7, input.getConditionEndDatetime());
    		pstConditionOccurrence.setObject(8, input.getConditionTypeConceptId());
    		pstConditionOccurrence.setObject(9, input.getConditionStatusConceptId());
    		pstConditionOccurrence.setObject(10, input.getStopReason());
    		pstConditionOccurrence.setObject(11, input.getProviderId());
    		pstConditionOccurrence.setObject(12, input.getVisitOccurrenceId());
    		pstConditionOccurrence.setObject(13, input.getVisitDetailId());
    		pstConditionOccurrence.setObject(14, input.getConditionSourceValue());
    		pstConditionOccurrence.setObject(15, input.getConditionSourceConceptId());
    		pstConditionOccurrence.setObject(16, input.getConditionStatusSourceValue());
    		return pstConditionOccurrence.executeUpdate();
    	}
	
		public int writeBatchConditionOccurrence(Collection<ConditionOccurrence> inputs) throws SQLException {
			return writeBatchConditionOccurrence(inputs,0);
		}
	
		public int writeBatchConditionOccurrence(Collection<ConditionOccurrence> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (ConditionOccurrence input: inputs) {
				pstConditionOccurrence.clearParameters();
    			pstConditionOccurrence.setObject(1, input.getConditionOccurrenceId());
    			pstConditionOccurrence.setObject(2, input.getPersonId());
    			pstConditionOccurrence.setObject(3, input.getConditionConceptId());
    			pstConditionOccurrence.setObject(4, input.getConditionStartDate());
    			pstConditionOccurrence.setObject(5, input.getConditionStartDatetime());
    			pstConditionOccurrence.setObject(6, input.getConditionEndDate());
    			pstConditionOccurrence.setObject(7, input.getConditionEndDatetime());
    			pstConditionOccurrence.setObject(8, input.getConditionTypeConceptId());
    			pstConditionOccurrence.setObject(9, input.getConditionStatusConceptId());
    			pstConditionOccurrence.setObject(10, input.getStopReason());
    			pstConditionOccurrence.setObject(11, input.getProviderId());
    			pstConditionOccurrence.setObject(12, input.getVisitOccurrenceId());
    			pstConditionOccurrence.setObject(13, input.getVisitDetailId());
    			pstConditionOccurrence.setObject(14, input.getConditionSourceValue());
    			pstConditionOccurrence.setObject(15, input.getConditionSourceConceptId());
    			pstConditionOccurrence.setObject(16, input.getConditionStatusSourceValue());
    			pstConditionOccurrence.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstConditionOccurrence.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstConditionOccurrence.executeBatch()).sum();
    		return affected;
		}
		public Consumer<Observation> ofObservation(final boolean rethrow) {
    		return new Consumer<Observation>() {
    			@Override
    			public void accept(Observation input) {
    				try {
    					writeObservation(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeObservation(Observation input) throws SQLException {
			pstObservation.clearParameters();
    		pstObservation.setObject(1, input.getObservationId());
    		pstObservation.setObject(2, input.getPersonId());
    		pstObservation.setObject(3, input.getObservationConceptId());
    		pstObservation.setObject(4, input.getObservationDate());
    		pstObservation.setObject(5, input.getObservationDatetime());
    		pstObservation.setObject(6, input.getObservationTypeConceptId());
    		pstObservation.setObject(7, input.getValueAsNumber());
    		pstObservation.setObject(8, input.getValueAsString());
    		pstObservation.setObject(9, input.getValueAsConceptId());
    		pstObservation.setObject(10, input.getQualifierConceptId());
    		pstObservation.setObject(11, input.getUnitConceptId());
    		pstObservation.setObject(12, input.getProviderId());
    		pstObservation.setObject(13, input.getVisitOccurrenceId());
    		pstObservation.setObject(14, input.getVisitDetailId());
    		pstObservation.setObject(15, input.getObservationSourceValue());
    		pstObservation.setObject(16, input.getObservationSourceConceptId());
    		pstObservation.setObject(17, input.getUnitSourceValue());
    		pstObservation.setObject(18, input.getQualifierSourceValue());
    		pstObservation.setObject(19, input.getObservationEventId());
    		pstObservation.setObject(20, input.getObsEventFieldConceptId());
    		pstObservation.setObject(21, input.getValueAsDatetime());
    		return pstObservation.executeUpdate();
    	}
	
		public int writeBatchObservation(Collection<Observation> inputs) throws SQLException {
			return writeBatchObservation(inputs,0);
		}
	
		public int writeBatchObservation(Collection<Observation> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (Observation input: inputs) {
				pstObservation.clearParameters();
    			pstObservation.setObject(1, input.getObservationId());
    			pstObservation.setObject(2, input.getPersonId());
    			pstObservation.setObject(3, input.getObservationConceptId());
    			pstObservation.setObject(4, input.getObservationDate());
    			pstObservation.setObject(5, input.getObservationDatetime());
    			pstObservation.setObject(6, input.getObservationTypeConceptId());
    			pstObservation.setObject(7, input.getValueAsNumber());
    			pstObservation.setObject(8, input.getValueAsString());
    			pstObservation.setObject(9, input.getValueAsConceptId());
    			pstObservation.setObject(10, input.getQualifierConceptId());
    			pstObservation.setObject(11, input.getUnitConceptId());
    			pstObservation.setObject(12, input.getProviderId());
    			pstObservation.setObject(13, input.getVisitOccurrenceId());
    			pstObservation.setObject(14, input.getVisitDetailId());
    			pstObservation.setObject(15, input.getObservationSourceValue());
    			pstObservation.setObject(16, input.getObservationSourceConceptId());
    			pstObservation.setObject(17, input.getUnitSourceValue());
    			pstObservation.setObject(18, input.getQualifierSourceValue());
    			pstObservation.setObject(19, input.getObservationEventId());
    			pstObservation.setObject(20, input.getObsEventFieldConceptId());
    			pstObservation.setObject(21, input.getValueAsDatetime());
    			pstObservation.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstObservation.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstObservation.executeBatch()).sum();
    		return affected;
		}
		public Consumer<Measurement> ofMeasurement(final boolean rethrow) {
    		return new Consumer<Measurement>() {
    			@Override
    			public void accept(Measurement input) {
    				try {
    					writeMeasurement(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeMeasurement(Measurement input) throws SQLException {
			pstMeasurement.clearParameters();
    		pstMeasurement.setObject(1, input.getMeasurementId());
    		pstMeasurement.setObject(2, input.getPersonId());
    		pstMeasurement.setObject(3, input.getMeasurementConceptId());
    		pstMeasurement.setObject(4, input.getMeasurementDate());
    		pstMeasurement.setObject(5, input.getMeasurementDatetime());
    		pstMeasurement.setObject(6, input.getMeasurementTime());
    		pstMeasurement.setObject(7, input.getMeasurementTypeConceptId());
    		pstMeasurement.setObject(8, input.getOperatorConceptId());
    		pstMeasurement.setObject(9, input.getValueAsNumber());
    		pstMeasurement.setObject(10, input.getValueAsConceptId());
    		pstMeasurement.setObject(11, input.getUnitConceptId());
    		pstMeasurement.setObject(12, input.getRangeLow());
    		pstMeasurement.setObject(13, input.getRangeHigh());
    		pstMeasurement.setObject(14, input.getProviderId());
    		pstMeasurement.setObject(15, input.getVisitOccurrenceId());
    		pstMeasurement.setObject(16, input.getVisitDetailId());
    		pstMeasurement.setObject(17, input.getMeasurementSourceValue());
    		pstMeasurement.setObject(18, input.getMeasurementSourceConceptId());
    		pstMeasurement.setObject(19, input.getUnitSourceValue());
    		pstMeasurement.setObject(20, input.getValueSourceValue());
    		return pstMeasurement.executeUpdate();
    	}
	
		public int writeBatchMeasurement(Collection<Measurement> inputs) throws SQLException {
			return writeBatchMeasurement(inputs,0);
		}
	
		public int writeBatchMeasurement(Collection<Measurement> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (Measurement input: inputs) {
				pstMeasurement.clearParameters();
    			pstMeasurement.setObject(1, input.getMeasurementId());
    			pstMeasurement.setObject(2, input.getPersonId());
    			pstMeasurement.setObject(3, input.getMeasurementConceptId());
    			pstMeasurement.setObject(4, input.getMeasurementDate());
    			pstMeasurement.setObject(5, input.getMeasurementDatetime());
    			pstMeasurement.setObject(6, input.getMeasurementTime());
    			pstMeasurement.setObject(7, input.getMeasurementTypeConceptId());
    			pstMeasurement.setObject(8, input.getOperatorConceptId());
    			pstMeasurement.setObject(9, input.getValueAsNumber());
    			pstMeasurement.setObject(10, input.getValueAsConceptId());
    			pstMeasurement.setObject(11, input.getUnitConceptId());
    			pstMeasurement.setObject(12, input.getRangeLow());
    			pstMeasurement.setObject(13, input.getRangeHigh());
    			pstMeasurement.setObject(14, input.getProviderId());
    			pstMeasurement.setObject(15, input.getVisitOccurrenceId());
    			pstMeasurement.setObject(16, input.getVisitDetailId());
    			pstMeasurement.setObject(17, input.getMeasurementSourceValue());
    			pstMeasurement.setObject(18, input.getMeasurementSourceConceptId());
    			pstMeasurement.setObject(19, input.getUnitSourceValue());
    			pstMeasurement.setObject(20, input.getValueSourceValue());
    			pstMeasurement.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstMeasurement.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstMeasurement.executeBatch()).sum();
    		return affected;
		}
		public Consumer<Note> ofNote(final boolean rethrow) {
    		return new Consumer<Note>() {
    			@Override
    			public void accept(Note input) {
    				try {
    					writeNote(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeNote(Note input) throws SQLException {
			pstNote.clearParameters();
    		pstNote.setObject(1, input.getNoteId());
    		pstNote.setObject(2, input.getPersonId());
    		pstNote.setObject(3, input.getNoteEventId());
    		pstNote.setObject(4, input.getNoteEventFieldConceptId());
    		pstNote.setObject(5, input.getNoteDate());
    		pstNote.setObject(6, input.getNoteDatetime());
    		pstNote.setObject(7, input.getNoteTypeConceptId());
    		pstNote.setObject(8, input.getNoteClassConceptId());
    		pstNote.setObject(9, input.getNoteTitle());
    		pstNote.setObject(10, input.getNoteText());
    		pstNote.setObject(11, input.getEncodingConceptId());
    		pstNote.setObject(12, input.getLanguageConceptId());
    		pstNote.setObject(13, input.getProviderId());
    		pstNote.setObject(14, input.getVisitOccurrenceId());
    		pstNote.setObject(15, input.getVisitDetailId());
    		pstNote.setObject(16, input.getNoteSourceValue());
    		return pstNote.executeUpdate();
    	}
	
		public int writeBatchNote(Collection<Note> inputs) throws SQLException {
			return writeBatchNote(inputs,0);
		}
	
		public int writeBatchNote(Collection<Note> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (Note input: inputs) {
				pstNote.clearParameters();
    			pstNote.setObject(1, input.getNoteId());
    			pstNote.setObject(2, input.getPersonId());
    			pstNote.setObject(3, input.getNoteEventId());
    			pstNote.setObject(4, input.getNoteEventFieldConceptId());
    			pstNote.setObject(5, input.getNoteDate());
    			pstNote.setObject(6, input.getNoteDatetime());
    			pstNote.setObject(7, input.getNoteTypeConceptId());
    			pstNote.setObject(8, input.getNoteClassConceptId());
    			pstNote.setObject(9, input.getNoteTitle());
    			pstNote.setObject(10, input.getNoteText());
    			pstNote.setObject(11, input.getEncodingConceptId());
    			pstNote.setObject(12, input.getLanguageConceptId());
    			pstNote.setObject(13, input.getProviderId());
    			pstNote.setObject(14, input.getVisitOccurrenceId());
    			pstNote.setObject(15, input.getVisitDetailId());
    			pstNote.setObject(16, input.getNoteSourceValue());
    			pstNote.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstNote.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstNote.executeBatch()).sum();
    		return affected;
		}
		public Consumer<CuiOmopMap> ofCuiOmopMap(final boolean rethrow) {
    		return new Consumer<CuiOmopMap>() {
    			@Override
    			public void accept(CuiOmopMap input) {
    				try {
    					writeCuiOmopMap(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int writeCuiOmopMap(CuiOmopMap input) throws SQLException {
			pstCuiOmopMap.clearParameters();
    		pstCuiOmopMap.setObject(1, input.getCui());
    		pstCuiOmopMap.setObject(2, input.getSourceConceptId());
    		pstCuiOmopMap.setObject(3, input.getConceptId());
    		return pstCuiOmopMap.executeUpdate();
    	}
	
		public int writeBatchCuiOmopMap(Collection<CuiOmopMap> inputs) throws SQLException {
			return writeBatchCuiOmopMap(inputs,0);
		}
	
		public int writeBatchCuiOmopMap(Collection<CuiOmopMap> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (CuiOmopMap input: inputs) {
				pstCuiOmopMap.clearParameters();
    			pstCuiOmopMap.setObject(1, input.getCui());
    			pstCuiOmopMap.setObject(2, input.getSourceConceptId());
    			pstCuiOmopMap.setObject(3, input.getConceptId());
    			pstCuiOmopMap.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pstCuiOmopMap.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pstCuiOmopMap.executeBatch()).sum();
    		return affected;
		}
	}
	
	/*************** SQL QUERIES **************************/

	public Query query() throws SQLException {
		if (query == null) query = new Query(conn);
		return query;
	}
	
	Query query;

	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public class Query {
	
		PreparedStatement pstInput;
		 
	
		Query(Connection conn) throws SQLException {
			pstInput = conn.prepareStatement("SELECT TOP(1) * from omopBuild.dbo.NlpWorklist where nlp_system=?");
		}
	
	
		
		public Iterator<Input> fromInput(String param1) throws SQLException  {
    		pstInput.setObject(1, param1);
    		ResultSet rs = pstInput.executeQuery();
    		return iterateResultSet(rs,r -> new InputSql(rs));
    	}
	
		public Stream<Input> streamInput(String param1) throws SQLException {
    		pstInput.setObject(1, param1);
    		ResultSet rs = pstInput.executeQuery();
    		return streamResultSet(rs,r -> new InputSql(rs));
		}
	
	}
}
