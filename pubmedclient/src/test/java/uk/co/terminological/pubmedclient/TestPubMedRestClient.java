package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;
import uk.co.terminological.pubmedclient.PubMedResult.Search;

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
		
		PubMedRestClient restClient = PubMedRestClient.create(pubmedApiToken, appId, developerEmail);
			
		Search result = restClient.createESearchQuery()
			.searchTerm("Doxapram")
			.limit(0, 10)
			.execute();
		
		result.getIds().forEach(System.out::println);
		
		/*restClient
				.searchPubmedByTitle("Anaesthetic influences on brain haemodynamics in the rat and their significance to biochemical, neuropharmacological and drug disposition studies.");
		Optional<PubmedArticle> pubmedArticle = restClient.fetchPubmedEntry("2764997");
		pubmedArticle.ifPresent(a -> logger.info(a.getMedlineCitation().getPMID().getvalue()));
		
		MeshHeadingList mesHeadingList = restClient.fetchMeshHeadingsForPubmedArticle(2764997L);
			for (MeshHeading meshHeading : mesHeadingList.getMeshHeading()) {
				for (QualifierName qualifierName : meshHeading.getQualifierName()) {
					logger.info("{} ({})/{} ({})",
							new Object[] { meshHeading.getDescriptorName().getvalue(),
									meshHeading.getDescriptorName().getMajorTopicYN(), qualifierName.getvalue(),
									qualifierName.getMajorTopicYN() });
				}
			}
			restClient
					.seachPubmedCentral("Accuracy of single progesterone test to predict early pregnancy outcome in women with pain or bleeding: meta-analysis of cohort studies");
			restClient
					.seachPubmedCentralByTitle("Accuracy of single progesterone test to predict early pregnancy outcome in women with pain or bleeding: meta-analysis of cohort studies");
			//restClient.fetchFullTextArticle("3460254");
		}*/
	}

}
