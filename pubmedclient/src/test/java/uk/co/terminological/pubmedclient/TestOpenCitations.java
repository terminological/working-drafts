package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.bibliography.BibliographicApiException;
import uk.co.terminological.bibliography.opencitations.OpenCitationsClient;

public class TestOpenCitations {

	static String[] dois = {
			"10.3115/990820.990850"
	};
	
	static String DEVELOPER = "test@example.org";
	
	public static void main(String[] args) throws BibliographicApiException, IOException {
		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		//Path tmp = Files.createTempDirectory("test");
		
		OpenCitationsClient xref = OpenCitationsClient.create();
		xref.debugMode();
		
		for (int i=0; i<5; i++) {
			
		
			for (String ref: dois) {
				System.out.println(ref);
				List<String> referenced = xref.getReferencedDoisByDoi(ref); 
				List<String> referencing = xref.getReferencingDoisByDoi(ref);
				System.out.println("Cites:");
				referencing.forEach(System.out::println);
				System.out.println("Cited by:");
				referenced.forEach(System.out::println);
			}
			
		}
	}

}
