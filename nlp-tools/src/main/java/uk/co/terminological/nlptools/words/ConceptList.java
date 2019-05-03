package uk.co.terminological.nlptools.words;

import java.util.ArrayList;

public class ConceptList extends ArrayList<Concept> {

	private DescriptionDictionary descDict;
	private NGramDictionary dict;
	
	protected DescriptionDictionary getDescDict() {
		return descDict;
	}
	protected NGramDictionary getDict() {
		return dict;
	}
	
	
}
