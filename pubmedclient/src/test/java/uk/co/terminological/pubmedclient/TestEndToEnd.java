package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import uk.co.terminological.pubmedclient.IdConverterClient.IdType;

public class TestEndToEnd {

	public static void main(String[] args) throws IOException, BibliographicApiException, ParseException {
		
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
		
		EntrezResult.Search result = pubmed.buildSearchQuery("machine learning").limit(0, 50).betweenDates(
				LocalDate.of(2016, 01, 1), 
				LocalDate.of(2017, 01, 1)).execute();
		
		List<String> dois = mapper.getDoisByIdAndType(result.getIds(), IdType.PMID);

		unpaywall.getUnpaywallByDois(dois).stream()
			.forEach(res -> System.out.println(res.doi.get()+"\t"+res.title.get()+"\t"+res.pdfUrl()));
		
	}

}
