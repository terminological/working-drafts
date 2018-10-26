package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class TestEndToEnd {

	public static void main(String[] args) throws IOException {
		
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(Paths.get(prop.getProperty("user.home"),"Dropbox/secrets.prop")));
		
		String xrefToken = prop.getProperty("crossref.clickthroughtoken");
		String developerEmail = prop.getProperty("developeremail");
		
		String pubmedApiToken = prop.getProperty("pubmed.apikey");
		String appId = prop.getProperty("appid");
		
		
		

	}

}
