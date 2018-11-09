package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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
				LocalDate.of(2017, 01, 1)).execute().get();
		
		List<String> dois2 = pubmed.getPMEntriesByPMIds(result.getIds()).stream().flatMap(pme -> pme.getDoi().stream()).collect(Collectors.toList());
		List<String> dois = mapper.getDoisByIdAndType(result.getIds(), IdType.PMID);

		List<String> tmp = new ArrayList<>(dois2);
		tmp.removeAll(dois);
		System.out.println("Extra dois found by pubmed");
		tmp.forEach(System.out::println);
		
		List<String> tmp2 = new ArrayList<>(dois);
		tmp2.removeAll(dois2);
		System.out.println("Extra dois found by id mapper");
		tmp2.forEach(System.out::println);
		
		
		System.out.println("Unpaywall entries");
		unpaywall.getUnpaywallByDois(dois2).stream()
			.forEach(res -> System.out.println(res.doi.get()+"\t"+res.title.get()+"\t"+res.pdfUrl()));
		
		//CrossRefResult.Work work = xref.getByDoi(dois2.get(0)).work.get();
		//https://academic.oup.com/bioinformatics/article-pdf/33/6/863/25147932/btw768.pdf
		CrossRefResult.Work work = xref.getByDoi("10.1093/bioinformatics/btw768").work.get();
		work.title.forEach(System.out::println);
		//System.out.println(work.journalAbstract.get());
		work.reference.forEach(r -> System.out.println(r.DOI+"\t"+r.articleTitle));
		
	}

}
