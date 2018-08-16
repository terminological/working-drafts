package uk.co.terminological.deid;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Records;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.pipestream.Event;
import uk.co.terminological.pipestream.EventBus;
import uk.co.terminological.pipestream.EventSerializer;
import uk.co.terminological.pipestream.FileUtils.DeferredInputStream;
import uk.co.terminological.pipestream.FileUtils.DirectoryScanner;
import uk.co.terminological.pipestream.FluentEvents.Events;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.HandlerTypes.Adaptor;
import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
import uk.co.terminological.pipestream.HandlerTypes.Processor;
import uk.co.terminological.pipestream.HandlerTypes.Terminal;


public class I2B2Experiment {


	//Event types
	public static final String ARCHIVE_FILE_FOUND = "ARCHIVE_FILE_FOUND";
	public static final String ARCHIVE_FILE_READY = "ARCHIVE_FILE_READY";
	public static final String XML_READY = "XML_READY";
	private static final String COMMON_FORMAT_RECORD_READY = "COMMON_FORMAT_RECORD_READY";
	private static final String BRAT_FORMAT_READY = "BRAT_FORMAT_READY";
	
	//Handler names
	private static final String ARCHIVE_LOADER = "ARCHIVE_LOADER";
	private static final String TAR_TO_XML = "TAR_TO_XML";
	private static final String I2B2_2014_TO_COMMON = "I2B2_2014_TO_COMMON";
	private static final String I2B2_2006_TO_COMMON = "I2B2_2006_TO_COMMON";
	private static final String COMMON_FORMAT_TO_BRAT = "COMMON_FORMAT_TO_BRAT";
	
	//Event names
	private static final String I2B2_2014_FORMAT = "I2B2_2014_FORMAT";
	private static final String I2B2_2006_FORMAT = "I2B2_2006_FORMAT";
	
	//Event metadata key names
	private static final String XML_FILENAME = "XML_FILENAME";
	private static final String BRAT_FORMAT_WRITER = "BRAT_WRITER";
	private static final String COMMON_FORMAT_TYPE_AGGREGATOR = "TYPE_COUNTER";
	
	
	
	
	
	
	
	public static void main(String args[]) {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		Path inputDir1 = Paths.get("/media/data/Data/i2b2/2014Track1");
		Path inputDir2 = Paths.get("/media/data/Data/i2b2/2006Set1B");
		Path outputDir = Paths.get("/media/data/Data/i2b2/brat");
		String countFilename = "counts.txt";
		EventBus.get()
			.withApi(new CommonFormatConverter())
			.withEventGenerator(tarGzFinder(inputDir1,I2B2_2014_FORMAT))
			.withEventGenerator(zipFinder(inputDir2,I2B2_2006_FORMAT))
			.withHandler(tarGzLoader(I2B2_2014_FORMAT))
			.withHandler(zipLoader(I2B2_2006_FORMAT))
			.withHandler(xmlFromArchive(I2B2_2014_FORMAT,I2B2_2014_FORMAT))
			.withHandler(xmlFromArchive(I2B2_2006_FORMAT,I2B2_2006_FORMAT))
			.withHandler(commonFormatFrom2006Xml())
			.withHandler(commonFormatFrom2014Xml())
			.withHandler(commonFormatAggregator(outputDir.resolve(countFilename)))
			.withHandler(bratFormatFromCommon())
			.withHandler(bratFormatWriter(outputDir))
			.debugMode()
			.execute()
			.sendShutdownMessage()
			.shutdown();


	}


	static DirectoryScanner tarGzFinder(Path directory, String zipType) {
		return Generators.directoryScanner(directory, 
				file -> file.getAbsolutePath().endsWith(".tar.gz"), 
				zipType, ARCHIVE_FILE_FOUND);
	}

	static Adaptor<Path,DeferredInputStream<ArchiveInputStream>> tarGzLoader(String zipType) {
		return Handlers.adaptor(ARCHIVE_LOADER,

				Predicates.matchNameAndType(zipType, ARCHIVE_FILE_FOUND), 

				p -> DeferredInputStream.create(p, 
						p2 -> new TarArchiveInputStream(
								new GzipCompressorInputStream(
										Files.newInputStream(p2)))),

				name -> zipType,
				type -> ARCHIVE_FILE_READY);
	}

	static DirectoryScanner zipFinder(Path directory, String zipType) {
		return Generators.directoryScanner(directory, 
				file -> file.getAbsolutePath().endsWith(".zip"), 
				zipType, ARCHIVE_FILE_FOUND);
	}

	static Adaptor<Path,DeferredInputStream<ArchiveInputStream>> zipLoader(String zipType) {
		return Handlers.adaptor(ARCHIVE_LOADER,

				Predicates.matchNameAndType(zipType, ARCHIVE_FILE_FOUND), 

				p -> DeferredInputStream.create(p, 
						p2 -> new ZipArchiveInputStream(
										Files.newInputStream(p2))),

				name -> zipType,
				type -> ARCHIVE_FILE_READY);
	}
	
	static Processor<DeferredInputStream<ArchiveInputStream>> xmlFromArchive(String zipType, String xmlType) {
		return Handlers.processor(TAR_TO_XML,
				Predicates.matchNameAndType(zipType, ARCHIVE_FILE_READY), 
				(zip, context) -> {
					ArchiveInputStream ais = zip.get();
					ArchiveEntry entry;
					try {
						while ((entry = ais.getNextEntry()) != null) {
							if (entry.getName().endsWith(".xml")) {
								InputStream tmp = new FilterInputStream(ais) {
									@Override
									public void close() throws IOException {}
								};
								Xml out;
								try {
									out = Xml.fromStream(tmp);
									context.send(
											Events.namedTypedEvent(out, 
													xmlType, 
													XML_READY).put(XML_FILENAME, entry.getName())
											);
								} catch (XmlException e) {
									context.getEventBus().logError("Cannot parse XML file:" +entry.getName());
									context.getEventBus().handleException(e);
								}

							}
						}
					} catch (IOException e) {
						context.getEventBus().logError("Cannot read next zip entry");
						context.getEventBus().handleException(e);
					}
				});
	}

	static EventProcessor<Xml> commonFormatFrom2014Xml() {
		return Handlers.eventProcessor(I2B2_2014_TO_COMMON, 
				Predicates.matchNameAndType(I2B2_2014_FORMAT, XML_READY), 
				(event, context) -> {
					try {
						Xml xml = event.get();
						Record record = 
								context.getEventBus().getApi(CommonFormatConverter.class).get()
								.fromI2B2_2014_Xml(xml, event.get(XML_FILENAME).toString());
						context.send(
								Events.namedTypedEvent(record, 
										record.id, 
										COMMON_FORMAT_RECORD_READY));
					} catch (XmlException e) {
						context.getEventBus().handleException(e);
					}
				});
	}

	static EventProcessor<Xml> commonFormatFrom2006Xml() {
		return Handlers.eventProcessor(I2B2_2006_TO_COMMON, 
				Predicates.matchNameAndType(I2B2_2006_FORMAT, XML_READY), 
				(event, context) -> {
					try {
						Xml xml = event.get();
						Records records = 
								context.getEventBus().getApi(CommonFormatConverter.class).get()
								.fromI2B2_2006_Xml(xml, event.get(XML_FILENAME).toString());
						records.forEach(
								r -> context.send(
									Events.namedTypedEvent(r,r.id, 
										COMMON_FORMAT_RECORD_READY)));
					} catch (XmlException e) {
						context.getEventBus().handleException(e);
					}
				});
	}
	
	static Adaptor<CommonFormat.Record, BRATFormat> bratFormatFromCommon() {
		return Handlers.adaptor(COMMON_FORMAT_TO_BRAT, 
				Predicates.matchType(COMMON_FORMAT_RECORD_READY), 
				(record, context) -> context.getEventBus().getApi(CommonFormatConverter.class).get()
						.toBRATFormat(record),
				name -> name.getId(),
				type -> BRAT_FORMAT_READY	
				);
	}
	
	static Terminal<BRATFormat> bratFormatWriter(Path directory) {
		return Handlers.consumer(BRAT_FORMAT_WRITER, 
				Predicates.matchType(BRAT_FORMAT_READY), 
				brat -> {
					EventSerializer.TO_STRING_FILE_WRITER.write(
							brat.getDocumentText(), directory.resolve(brat.getId()+".txt"));
					EventSerializer.TO_STRING_FILE_WRITER.write(
							brat.getStandoffOutput(), directory.resolve(brat.getId()+".ann"));
				});
	}

	static EventProcessor<CommonFormat.Record> commonFormatAggregator(Path file) {
		return new EventProcessor<CommonFormat.Record>(COMMON_FORMAT_TYPE_AGGREGATOR) {

			private Map<String,Integer> counter = FluentMap.create();
			
			@Override
			public boolean canHandle(Event<?> event) {
				return Predicates.matchType(COMMON_FORMAT_RECORD_READY).or(
						Predicates.shutdown()).test(event);
			}

			@Override
			public void process(Event<Record> event, EventProcessor<Record> context) {
				if (Predicates.shutdown().test(event)) {
					
					StringBuilder toTsv = new StringBuilder();
					counter.entrySet().stream().forEach( (kv) -> {
						toTsv.append(kv.getKey()+"\t"+kv.getValue()+"\n");
					});
					EventSerializer.TO_STRING_FILE_WRITER.write(
							toTsv.toString(), file);
					
				} else if (Predicates.matchType(COMMON_FORMAT_RECORD_READY).test(event)) {
					
					for (CommonFormat.Span span: event.get().spans) {
						if (span.type != null) {
							Integer count = counter.get(span.type);
							if (count == null) count = 0;
							count += 1;
							counter.put(span.type, count);
						}
						if (span.subtype != null) {
							Integer count = counter.get(span.subtype);
							if (count == null) count = 0;
							count += 1;
							counter.put(span.subtype, count);
						}
					}
				}
			}
		};
	}
	
}
	
