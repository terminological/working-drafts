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

import uk.co.terminological.fluentxml.Xml;
import uk.co.terminological.fluentxml.XmlException;
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
				file -> file.getAbsolutePath().endsWith(".tar.gz"), 
				zipType, "TAR_FILE_FOUND");
	}
	
	Adaptor<Path,DeferredInputStream<ArchiveInputStream>> zipLoader(Path file, String zipType) {
		return Handlers.adaptor(
				
				Predicates.matchNameAndType(zipType, "TAR_FILE_FOUND"), 
	
				p -> DeferredInputStream.create(p, 
							p2 -> new TarArchiveInputStream(
									new GzipCompressorInputStream(
											Files.newInputStream(p2)))),
					
				name -> zipType,
				type -> "TAR_FILE_READY");
	}

	Processor<DeferredInputStream<ArchiveInputStream>> xmlGenerator(String zipType, String xmlType) {
		return Handlers.processor(
				Predicates.matchNameAndType(zipType, "TAR_FILE_READY"), 
				(zip, context) -> {
					ArchiveInputStream ais = zip.get();
					ArchiveEntry entry;
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
							        			"XML_LOADED")	
							        		);
							} catch (XmlException e) {
								context.getEventBus().logError("Cannot parse XML file:" +entry.getName());
								context.getEventBus().handleException(e);
							}
					        
					    }
					}
					 
					        
				}, 
				name -> xmlType, 
				type -> "XML_READY");
	}
	
}
