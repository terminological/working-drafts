package uk.co.terminological.nlptools.words;

import java.util.ArrayList;
import java.util.List;

public class Description {

	private String term;
	private Concept concept;
	private List<Ngram> ngrams;
	
	protected Description(String term, Concept concept) {
		this.term = term;
		this.ngrams = concept.getConceptList().getDict().create(this);
	}

	public String getTerm() {
		return term;
	}

}
