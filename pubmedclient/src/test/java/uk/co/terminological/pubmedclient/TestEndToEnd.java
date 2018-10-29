package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

public class TestEndToEnd {

	public static void main(String[] args) throws IOException, BibliographicApiException {
		
		BasicConfigurator.configure();
		
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(Paths.get(prop.getProperty("user.home"),"Dropbox/secrets.prop")));
		
		String xrefToken = prop.getProperty("crossref.clickthroughtoken");
		String developerEmail = prop.getProperty("developeremail");
		
		String pubmedApiToken = prop.getProperty("pubmed.apikey");
		String appId = prop.getProperty("appid");
		
		EntrezClient pubmed = EntrezClient.create(pubmedApiToken, appId, developerEmail);
		IdConverterClient mapper = IdConverterClient.create(appId,developerEmail);
		CrossRefClient xref = CrossRefClient.create(developerEmail);
		UnpaywallClient unpaywall = UnpaywallClient.create(developerEmail);
		
		pubmed.buildSearchQuery("machine learning").limit(0, 50).execute()
			;
		

	}

}
