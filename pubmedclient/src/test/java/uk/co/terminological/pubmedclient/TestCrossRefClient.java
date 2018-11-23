package uk.co.terminological.pubmedclient;

import org.apache.log4j.BasicConfigurator;

public class TestCrossRefClient {

	static String[] articles =  {
		"Collier,N., Nobata,C. and Tsujii,J. (2000) Extracting the names of genes and gene products with a hidden Markov model. In Proceedings of the 18th International Conference on Computational Lingustics (COLING'2003), Saarbrucken, Germany, pp. 201-207.",
		"Kulick,S., Liberman,M., Palmer,M. and Schein,A. (2003) Shallow semantic annotations of biomedical corpora for information extraction. In Proceedings of the Third Meeting of the Special Interest Group on Text Mining at ISMB 2003.",
		"Lafferty,J., McCallum,A. and Pereira,F. (2001) Conditional Random Fields: probabilistic models for segmenting and labeling sequence data. In Proceedings of ICML-01, pp. 282-289."
	};
	
	
	static String DEVELOPER = "test@example.org";
	
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		
		CrossRefClient xref = CrossRefClient.create(DEVELOPER);
		for (String ref: articles) {
			System.out.println(ref);
			xref.findWorkByCitationString(ref).ifPresent(w -> w.title.forEach(System.out::println));
			System.out.println();
		}
			

	}

}
