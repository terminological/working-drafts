package uk.co.terminological.nlptools.words;

import java.util.ArrayList;
import java.util.List;

public class Concept {

	private ConceptList conceptList;
	private List<Description> descriptions = new ArrayList<>();
	protected ConceptList getConceptList() {
		return conceptList;
	}
	protected List<Description> getDescriptions() {
		return descriptions;
	}
	
}
