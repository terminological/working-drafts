package uk.co.terminological.deid;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.BasicConfigurator;

import uk.co.terminological.deid.CommonFormat.Record;
import uk.co.terminological.deid.CommonFormat.Span;
import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.fluentxml.XmlText;
import uk.co.terminological.pipestream.FileUtils.DeferredInputStream;
import uk.co.terminological.pipestream.FileUtils.DirectoryScanner;
import uk.co.terminological.pipestream.FluentEvents.Events;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.Handlers.Adaptor;
import uk.co.terminological.pipestream.Handlers.Processor;


public class I2B2Experiment {
	
	
	public static void main(String args[]) {
		
		BasicConfigurator.configure();
		
		
		
	}
	
	
	DirectoryScanner zipFinder(Path directory, String zipType) {
		return Generators.directoryScanner(directory, 
				file -> (file.getAbsolutePath().endsWith(".tar.gz") ||
						file.getAbsolutePath().endsWith(".zip")), 
				zipType, "ARCHIVE_FILE_FOUND");
	}
	
	Adaptor<Path,DeferredInputStream<ArchiveInputStream>> zipLoader(Path file, String zipType) {
		return Handlers.adaptor("ARCHIVE_LOADER",
				
				Predicates.matchNameAndType(zipType, "ARCHIVE_FILE_FOUND"), 
	
				p -> DeferredInputStream.create(p, 
							p2 -> new TarArchiveInputStream(
									new GzipCompressorInputStream(
											Files.newInputStream(p2)))),
					
				name -> zipType,
				type -> "ARCHIVE_FILE_READY");
	}

	Processor<DeferredInputStream<ArchiveInputStream>> xmlFromZip(String zipType, String xmlType) {
		return Handlers.processor("TAR_TO_XML",
				Predicates.matchNameAndType(zipType, "ARCHIVE_FILE_READY"), 
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
								        			"XML_READY").put("filename", entry.getName())
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
	
	Processor<Xml> commonFormatFrom2014Xml() {
		return Handlers.processor("I2B2_2014_TO_COMMON", 
				Predicates.matchNameAndType("I2B2_2014_FORMAT", "XML_READY"), 
				(event, context) -> {
					Xml xml = event.get();
					Record record;
					record.id = id;
					record.documentText = xml.doXpath("/deIdi2b2/TEXT[1]/text()").getOne(XmlText.class).getValue();
					for (XmlElement tags: xml.doXpath("/deIdi2b2/TAGS/*").getMany(XmlElement.class)) {
						record.spans.add(
							Span.from(
								Integer.parseInt(tags.getAsElement().getAttribute("start")),
								Integer.parseInt(tags.getAsElement().getAttribute("end")), 
								tags.getName(),
								tags.getAsElement().getAttribute("TYPE")));
					}
					context.send(Events.namedTypedEvent(record, record.id, "COMMON_FORMAT_AVAILABLE"));
					
				});
		
	}
	
	
	
}
