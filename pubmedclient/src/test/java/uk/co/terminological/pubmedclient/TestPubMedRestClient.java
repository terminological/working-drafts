package uk.co.terminological.pubmedclient;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeading;
import gov.nih.nlm.ncbi.eutils.generated.efetch.MeshHeadingList;
import gov.nih.nlm.ncbi.eutils.generated.efetch.PubmedArticle;
import gov.nih.nlm.ncbi.eutils.generated.efetch.QualifierName;

public class TestPubMedRestClient {

	public static Logger logger = LoggerFactory.getLogger(TestPubMedRestClient.class);
	public static String APP_ID = "test_client";
	public static String DEVELOPER_EMAIL = "rob@terminological.co.uk";
	
	public static void main(String[] args)  throws JAXBException {
		BasicConfigurator.configure();
		
		// Taunton[Affiliation] AND UK[Affiliation] AND NHS[Affilitation] 
		
		PubMedRestClient restClient = PubMedRestClient.create(args[0], APP_ID, DEVELOPER_EMAIL);
			
		restClient.searchPubmed("Doxapram", 0, 10);
		restClient
				.searchPubmedByTitle("Anaesthetic influences on brain haemodynamics in the rat and their significance to biochemical, neuropharmacological and drug disposition studies.");
		List<PubmedArticle> pubmedArticle = restClient.fetchPubmedArticle(Collections.singletonList("2764997"));
		pubmedArticle.stream().forEach(a -> logger.info(a.getMedlineCitation().getPMID().getvalue()));
		
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
		}

}
