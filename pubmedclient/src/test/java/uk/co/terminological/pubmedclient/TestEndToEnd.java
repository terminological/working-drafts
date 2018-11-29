package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.BasicConfigurator;



import uk.co.terminological.datatypes.StreamExceptions;
import uk.co.terminological.pubmedclient.IdConverterClient.IdType;

public class TestEndToEnd {

	public static void main(String[] args) throws IOException, BibliographicApiException, ParseException {
		
		BasicConfigurator.configure();
		
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(Paths.get(prop.getProperty("user.home"),"Dropbox/secrets.prop")));
		
		//String xrefToken = prop.getProperty("crossref.clickthroughtoken");
		String developerEmail = prop.getProperty("developer-email");
		String pubmedApiToken = prop.getProperty("pubmed-apikey");
		String appId = prop.getProperty("app-id");
		
		
		Path ehcache = Paths.get("/tmp/ehcache/test123");
		Files.createDirectories(ehcache.getParent());
		
		EntrezClient pubmed = EntrezClient.create(pubmedApiToken, appId, developerEmail);
		pubmed.withCache(ehcache.resolve("pubmed"));
		IdConverterClient mapper = IdConverterClient.create(appId,developerEmail, ehcache.resolve("idconv"));
		CrossRefClient xref = CrossRefClient.create(developerEmail, ehcache.resolve("xref"));
		UnpaywallClient unpaywall = UnpaywallClient.create(developerEmail, ehcache.resolve("unpaywall"));
		
		EntrezResult.Search result = pubmed.buildSearchQuery("machine learning").limit(0, 50).betweenDates(
				LocalDate.of(2016, 01, 1), 
				LocalDate.of(2017, 01, 1)).execute().get();
		
		List<String> dois2 = pubmed.getPMEntriesByPMIds(result.getIds().collect(Collectors.toList())).stream().flatMap(pme -> pme.getDoi().stream()).collect(Collectors.toList());
		Set<String> dois = mapper.getDoisByIdAndType(result.getIds().collect(Collectors.toList()), IdType.PMID);

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
		
		Stream<CrossRefResult.Work> work = dois2.stream().flatMap(doi -> {
			try {
				return xref.getByDoi(doi).stream();
			} catch (BibliographicApiException e) {
				return Stream.empty();
			}
		})
				.flatMap(sr -> sr.work.stream());
		work.forEach(w -> w.title.forEach(System.out::println));
		//https://academic.oup.com/bioinformatics/article-pdf/33/6/863/25147932/btw768.pdf
		CrossRefResult.Work work2 = xref.getByDoi("10.1093/bioinformatics/btw768").get().work.get();
		//System.out.println(work.journalAbstract.get());
		work2.reference.forEach(r -> System.out.println(r.DOI+"\t"+r.articleTitle));
		System.exit(0);
	}

}
