package uk.co.terminological.deid;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.BasicConfigurator;

import uk.co.terminological.pipestream.FileUtils.DeferredInputStream;
import uk.co.terminological.pipestream.FileUtils.DirectoryScanner;
import uk.co.terminological.pipestream.FluentEvents.Generators;
import uk.co.terminological.pipestream.FluentEvents.Handlers;
import uk.co.terminological.pipestream.FluentEvents.Predicates;
import uk.co.terminological.pipestream.Handlers.Adaptor;


public class I2B2Experiment {
	
	
	public static void main(String args[]) {
		
		BasicConfigurator.configure();
		
		
		
	}
	
	
	DirectoryScanner zipFinder(Path directory, String zipType) {
		return Generators.directoryScanner(directory, 
				file -> file.getAbsolutePath().endsWith(".xml"), 
				zipType, "ZIP_FILE_FOUND");
	}
	
	Adaptor<Path,DeferredInputStream<ArchiveInputStream>> zipLoader(Path file, String zipType) {
		return Handlers.adaptor(
				
				Predicates.matchNameAndType(zipType, "ZIP_FILE_FOUND"), 
	
				p -> DeferredInputStream.create(p, 
							p2 -> new TarArchiveInputStream(
									new GzipCompressorInputStream(
											Files.newInputStream(p2)))),
					
				name -> zipType,
				type -> "ZIP_FILE_READY");
	}

	
	
}
