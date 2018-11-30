package uk.co.terminological.nlptools;

import java.util.Comparator;
import java.util.HashMap;

public class Term {
	
	
	String tag;
	int count = 0;
	Corpus map;
	HashMap<Document,Integer> documentUsing = new HashMap<>();
	
	public Term(String tag, Corpus map) {
		this.tag = tag;
		this.map = map;
	}
	
	public void add(Document norm) {
		if (documentUsing.containsKey(norm)) {
			documentUsing.put(norm, documentUsing.get(norm)+1);
		} else {
			documentUsing.put(norm,1);
		}
		count+=1;
	}
	
	public int hashCode() {return tag.hashCode();}
	public boolean equals(Object o) {
		if (o instanceof Term) {
			return ((Term) o).tag.equals(tag);
		} else return false;
	}
	public String toString() {return tag+" ["+idf()+":"+count+"]";} 
	
	public int documentsWithTermCount() {return count;}
	public Double idf() {
		//probabilistic idf
		return Math.log(((double) map.corpusDocuments()-this.documentsWithTermCount())/this.documentsWithTermCount());
	}
	
}