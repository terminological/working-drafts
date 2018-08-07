package uk.co.terminological.pubmedclient;

import javax.xml.bind.JAXBException;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPubMedBatch {
	
	public static Logger logger = LoggerFactory.getLogger(TestPubMedRestClient.class);
	public static String APP_ID = "test_client";
	public static String DEVELOPER_EMAIL = "rob@terminological.co.uk";
	
	public static void main(String[] args)  throws JAXBException {
		BasicConfigurator.configure();
		
		PubMedBatchQuery.newSession(args[0], APP_ID, DEVELOPER_EMAIL)
			.search("Taunton[Affiliation] AND UK[Affiliation] AND NHS[Affilitation]")
			.forEachRemaining(
					b -> b.getTitles().stream().forEach(System.out::println)
				);
		
	}

}
