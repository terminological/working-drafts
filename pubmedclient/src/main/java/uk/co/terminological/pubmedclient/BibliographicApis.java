package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class BibliographicApis {

	// https://elibrary.exeter.ac.uk/idp/shibboleth
	// https://ssologin.exeter.ac.uk/distauth/UI/Login?realm=%2Fpeople
	// TODO: don't think this will really work as shibbolteh login requires going to login links on different publishers sites.

	// TODO: integrate CSL: https://michel-kraemer.github.io/citeproc-java/api/1.0.1/de/undercouch/citeproc/csl/CSLItemDataBuilder.html
	// TODO: Consider a google scholar scraper
	// TODO: https://www.crossref.org/labs/citation-formatting-service/
	// TODO: http://api.altmetric.com/
	// TODO: PDFBox or https://github.com/CeON/CERMINE
	// TODO: https://api.altmetric.com/v1/doi/10.1038/480426a

	// TODO: https://github.com/joelittlejohn/jsonschema2pojo/wiki/Getting-Started#the-maven-plugin
	
	// TODO: https://github.com/ncbi/JATSPreviewStylesheets
	// TODO: https://github.com/PeerJ/jats-conversion/blob/master/src/data/xsl/jats-to-html.xsl
	
	
	public static BibliographicApis create(Path filePath) throws IOException {

		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(filePath));

		String xrefToken = prop.getProperty("crossref.clickthroughtoken");
		String developerEmail = prop.getProperty("developeremail");

		String pubmedApiToken = prop.getProperty("pubmed.apikey");
		String appId = prop.getProperty("appid");
		
		Optional<Path> cacheDir = Optional.ofNullable(prop.getProperty("cache-directory")).map(s -> Paths.get(s));

		return create(appId,developerEmail,xrefToken,pubmedApiToken, cacheDir);

	}

	public static BibliographicApis create(String appId, String developerEmail, String xrefToken, String pubmedApiToken, Optional<Path> cacheDir) {
		return new BibliographicApis(appId, developerEmail, xrefToken, pubmedApiToken, cacheDir);
	}

	private EntrezClient entrez;
	private IdConverterClient pmcIdConv;
	private CachingApiClient crossref;
	private UnpaywallClient unpaywall;

	private BibliographicApis(String appId, String developerEmail, String xrefToken, String pubmedApiToken, Optional<Path> cacheDir) {

		entrez = EntrezClient.create(pubmedApiToken, appId, developerEmail);
		pmcIdConv = IdConverterClient.create(appId,developerEmail);
		crossref = CrossRefClient.create(developerEmail, cacheDir.map(d -> d.resolve("xref")));
		unpaywall = UnpaywallClient.create(developerEmail);

	}

	public BibliographicApis debugMode() {
		entrez.debugMode();
		pmcIdConv.debugMode();
		crossref.debugMode();
		unpaywall.debugMode();
		return this;
	}
	
	public EntrezClient getEntrez() {
		return entrez;
	}

	public IdConverterClient getPmcIdConv() {
		return pmcIdConv;
	}

	public CachingApiClient getCrossref() {
		return crossref;
	}

	public UnpaywallClient getUnpaywall() {
		return unpaywall;
	}

}
