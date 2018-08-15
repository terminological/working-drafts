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

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
import uk.co.terminological.pipestream.FileUtils.DeferredInputStream;
import uk.co.terminological.pipestream.FluentEvents.Events;
import uk.co.terminological.pipestream.Handlers.Processor;

public class I2B2ExperimentSteps {

	public static DeferredInputStream<ArchiveInputStream> loadArchive(Path p) {
		return DeferredInputStream.create(p, 
				p2 -> new TarArchiveInputStream(
						new GzipCompressorInputStream(
								Files.newInputStream(p2))));
	}
	
	
	public static void generateXmlFromZip(
			DeferredInputStream<ArchiveInputStream> zip, 
			Processor<?> context) {
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
							/*context.send(
						        	Events.namedTypedEvent(out, 
						        			xmlType, 
						        			"XML_LOADED")	
						        		);*/
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
		}
	}
	
