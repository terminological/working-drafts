package uk.co.terminological.pubmedclient;

import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pubmedclient.IdConverterClient.IdType;

public class TestConverterApiClient {

	
	public static Logger logger = LoggerFactory.getLogger(TestPubMedRestClient.class);
	public static String APP_ID = "test_client";
	public static String DEVELOPER_EMAIL = "rob@terminological.co.uk";
	
	public static void main(String[] args) throws BibliographicApiException {
		
		BasicConfigurator.configure();
		IdConverterClient client = new IdConverterClient(APP_ID,DEVELOPER_EMAIL).debugMode();
		client.getDoisByIdAndType(Arrays.asList("12964947"), IdType.PMID).forEach(System.out::println);
		
	}

}
