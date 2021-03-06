package uk.co.terminological.ctakes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.omop.Database;
import uk.co.terminological.omop.Factory;
import uk.co.terminological.omop.Input;
import uk.co.terminological.omop.NlpAudit;
import uk.co.terminological.omop.NoteNlp;

public class PipelineExecutor {

	

	public static void main(String[] args) {

		CtakesProperties p;
		if (args.length == 0) {
			p = new CtakesProperties();
		} else {
			p = new CtakesProperties(Paths.get(args[0]));
		}

		Logger log = LoggerFactory.getLogger(PipelineExecutor.class);
		checkpoint("Engine configuration loaded",log);

		Database db = new Database(p);
		JcasOmopMapper mapper = new JcasOmopMapper(db,p.nlpSystem());
		NlpPipeline ctakes = new NlpPipeline(p,true);

		checkpoint("Engine loaded and ready to parse",log);


		while (true) {


			Iterator<Input> input;
			try {
				try {
					input = db.query().fromInput(p.nlpSystem());
					if (!input.hasNext()) {
						Thread.sleep(1000);
						checkpoint("no input, idling.",log);
					} else {

						input.forEachRemaining(
								in -> {
									checkpoint("queueing note: "+in.getNoteId(),log);
									//System.out.println(in.getNoteText());
									try {

										try {
											NlpAudit start = Factory.Mutable.createNlpAudit()
													.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
													.withEventType(NlpPipeline.Status.PROCESSING)
													.withNlpSystem(p.nlpSystem())
													.withNlpSystemInstance(p.nlpSystemVersion())
													.withNoteId(in.getNoteId());
											db.write().writeNlpAudit(start);
										} catch (SQLException e) {
											//log - failed to grab the note - likely due to a clash for another system processing it.
											checkpoint("note is locked: "+in.getNoteId(),log);
											return;
										}

										checkpoint("processing note: "+in.getNoteId(),log);
										List<NlpAudit> outcomes = new ArrayList<>();
										try {
											List<NoteNlp> ret = ctakes.runNote(in, mapper);
											db.write().writeBatchNoteNlp(ret);

											outcomes.add(
													Factory.Mutable.createNlpAudit()
													.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
													.withEventType(NlpPipeline.Status.COMPLETE)
													.withNlpSystem(p.nlpSystem())
													.withNlpSystemInstance(p.nlpSystemVersion())
													.withNoteId(in.getNoteId()));

										} catch (Exception e) {

											checkpoint(p.nlpSystem()+" ("+p.nlpSystemVersion()+") failed, note: "+in.getNoteId()+", exception: "+e.getLocalizedMessage(),log);

											ByteArrayOutputStream baos = new ByteArrayOutputStream();
											PrintStream ps = new PrintStream(baos);
											e.printStackTrace(ps);
											outcomes.add(
													Factory.Mutable.createNlpAudit()
													.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
													.withEventType(NlpPipeline.Status.FAILED)
													.withNlpSystem(p.nlpSystem())
													.withNlpSystemInstance(p.nlpSystemVersion())
													.withNoteId(in.getNoteId())
													.withEventDetail(StringUtils.abbreviate(baos.toString(),512)));


											int retry = 0;
											if (in.getNlpEventType().equals(NlpPipeline.Status.RETRY))
												retry = Integer.parseInt(in.getNlpEventDetail());
											if (retry < CtakesProperties.MAX_RETRIES) {
												outcomes.add(
														Factory.Mutable.createNlpAudit()
														.withEventTime(Timestamp.valueOf(LocalDateTime.now()))
														.withEventType(NlpPipeline.Status.RETRY)
														.withNlpSystem(p.nlpSystem())
														.withNlpSystemInstance(p.nlpSystemVersion())
														.withNoteId(in.getNoteId())
														.withEventDetail(Integer.toString(retry+1)));
											}

										}

										db.write().writeBatchNlpAudit(outcomes);
									} catch (SQLException e) {
										//Problem writing audit log
										checkpoint(p.nlpSystem()+" ("+p.nlpSystemVersion()+") failed to write log for note: "+in.getNoteId()+", exception: "+e.getLocalizedMessage(),log);
										throw new RuntimeException(e);
									}
								}
								);
					}
				} catch (SQLException e1) {
					checkpoint("sql error getting new note - sleeping",log);
					Thread.sleep(1000);
				}
			} catch (InterruptedException e1) {
				checkpoint("main thread interrupted - shutting down",log);
				System.exit(0);
			}
		}
	}

	static long timestamp = System.currentTimeMillis(); 

	private static void checkpoint(String string,Logger log) {
		long timeTake = System.currentTimeMillis()-timestamp;
		log.warn("CHECKPOINT: "+timeTake+" ms: "+string);
		timestamp = System.currentTimeMillis();
	}

}
