package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pubmedclient.EntrezClient.Command;
import uk.co.terminological.pubmedclient.EntrezClient.Database;
import uk.co.terminological.pubmedclient.EntrezResult.Links;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;
import uk.co.terminological.pubmedclient.EntrezResult.Search;

public class TestPubMedRestClient {

	public static Logger logger = LoggerFactory.getLogger(TestPubMedRestClient.class);
	
	public static void main(String[] args) throws BibliographicApiException, IOException  {
		BasicConfigurator.configure();
		
		// Taunton[Affiliation] AND UK[Affiliation] AND NHS[Affilitation] 
		
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(Paths.get(prop.getProperty("user.home"),"Dropbox/secrets.prop")));
		
		String xrefToken = prop.getProperty("crossref.clickthroughtoken");
		String developerEmail = prop.getProperty("developeremail");
		
		String pubmedApiToken = prop.getProperty("pubmed.apikey");
		String appId = prop.getProperty("appid");
		
		EntrezClient restClient = EntrezClient.create(pubmedApiToken, appId, developerEmail);
			
		Search result = restClient.buildSearchQuery("Doxapram")
			.limit(0, 10)
			.execute();
		
		result.getIds().forEach(System.out::println);
		
		Links links = restClient.buildLinksQueryForIdsAndDatabase(result.getIds(), Database.PUBMED)
				.command(Command.PRLINKS)
				.execute();
		
		links.stream().forEach(System.out::println);
		
		Links links2 = restClient.buildLinksQueryForIdsAndDatabase(result.getIds(), Database.PUBMED)
				.command(Command.NEIGHBOR)
				.execute();
		
		links2.stream().forEach(System.out::println);
		
		PubMedEntries entries = restClient.getPMEntriesByPMIds(result.getIds());
		entries.getTitles().forEach(System.out::println);
		
		
		Optional<PubMedEntry> entry = restClient.getPMEntryByPMId("11748933");
		entry.get().getMeshHeadings().forEach(System.out::println);
		
		System.out.println(entry.get().getAbstract());
		
	}

}
