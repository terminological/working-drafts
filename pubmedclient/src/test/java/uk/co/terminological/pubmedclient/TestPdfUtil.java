package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.BasicConfigurator;

public class TestPdfUtil {

	public static String[] urls = {
			"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6227809/pdf/rsfs20180033.pdf",
			"https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6227809/pdf",
			"http://arxiv.org/pdf/1811.08430"
	};
	
	public static void main(String[] args) throws IOException, BibliographicApiException {

		BasicConfigurator.configure();
		Path tmp = Files.createTempDirectory("pdf_test");
		System.out.println(tmp.toString());
		
		for (int i=0; i<5; i++) {
			for (String url: urls) {
				PdfFetcher.create(tmp).getPdfFromUrl(url);
			}
		}
	}

}
