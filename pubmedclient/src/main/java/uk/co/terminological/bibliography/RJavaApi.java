package uk.co.terminological.bibliography;

import java.util.Arrays;
import java.util.List;

public class RJavaApi {
	//TODO: more task based methods 
	//TODO: 
	
	public List<String> getSupportedClients() {
		return Arrays.asList("crossref","entrez","europepmc","opencitations","pmcid","unpaywall");
	}
}
