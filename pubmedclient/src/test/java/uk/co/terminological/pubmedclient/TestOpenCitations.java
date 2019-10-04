package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.bibliography.BibliographicApiException;
import uk.co.terminological.bibliography.crossref.CrossRefClient;
import uk.co.terminological.bibliography.crossref.Work;
import uk.co.terminological.bibliography.opencitations.OpenCitationsClient;
import uk.co.terminological.bibliography.crossref.CrossRefClient.Field;
import uk.co.terminological.bibliography.crossref.CrossRefClient.QueryBuilder;
import uk.co.terminological.bibliography.crossref.CrossRefClient.Sort;
import uk.co.terminological.bibliography.crossref.CrossRefClient.SortOrder;

public class TestOpenCitations {

	static String[] dois = {
			"10.3115/990820.990850",
			null,
			null
	};
	
	static String DEVELOPER = "test@example.org";
	
	public static void main(String[] args) throws BibliographicApiException, IOException {
		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		//Path tmp = Files.createTempDirectory("test");
		
		OpenCitationsClient xref = OpenCitationsClient.create();
		xref.debugMode();
		
		QueryBuilder qb = xref.buildQuery()
				.withSearchTerm(Field.BIBLIOGRAPHIC, articles[0])
				.sortedBy(Sort.SCORE, SortOrder.DESC)
				.limit(1);
				
		for (int i=0; i<5; i++) {
			qb.execute();
		
		for (String ref: articles) {
			System.out.println(ref);
			Optional<Work> work = xref.findWorkByCitationString(ref); 
			work.ifPresent(
					w -> {
						System.out.println(w.getTitle());
						w.getCitedByCount().ifPresent(System.out::println);
						w.getIdentifier().ifPresent(System.out::println);
						w.getJournal().ifPresent(System.out::println);
					});
			System.out.println();
		}
			
		}
	}

}
