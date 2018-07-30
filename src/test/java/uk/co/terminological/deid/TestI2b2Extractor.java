package uk.co.terminological.deid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.management.openmbean.OpenMBeanOperationInfoSupport;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.BasicConfigurator;

import uk.co.terminological.fluentxml.XmlException;

public class TestI2b2Extractor {
	
	static final String INFILE = "/media/data/Data/i2b2/2014Track1/Track1-de-indentification.tar.gz";
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlException {
		BasicConfigurator.configure();
		I2b2Extractor extr = new I2b2Extractor();
		
		Writer out = Files.newBufferedWriter(Paths.get("/home/terminological/output.txt"));
		
		ArchiveInputStream ais = new TarArchiveInputStream(
			new GzipCompressorInputStream(
				new FileInputStream(INFILE)
			)
		);
		
		extr.convert(ais, out);
		
	}

}
