package uk.co.terminological.nlptools;

import java.util.HashSet;
import java.util.Set;

public class Term {
	
	String tag;
	Corpus corpus;
	Integer timesUsed;
	HashSet<Document> documentsUsing = new HashSet<>();
	
	public Term(String tag, Corpus map) {
		this.tag = tag;
		this.corpus = map;
	}
	
	public int hashCode() {return tag.hashCode();}
	public boolean equals(Object o) {
		if (o instanceof Term) {
			return ((Term) o).tag.equals(tag);
		} else return false;
	}
	public String toString() {return tag+" ["+idf()+"]";} 
	
	
	
	/*
	 * Add a new document to this term - called when a term is found in a document during the Document constructor
	 */
	protected void add(Document norm) {
		documentsUsing.add(norm);
	}
	
	
	public int countDocumentsWithTerm() {
		return documentsUsing.size();
	}
	
	/**
	 * The inverse document frequency is a measure of how much information the word provides, i.e., if it's common or rare across all documents. 
	 * <br/>
	 * It is the logarithmically scaled inverse fraction of the documents that contain the word 
	 * (obtained by dividing the total number of documents by the number of documents containing the term, and then taking the logarithm of that quotient):
	 * <br/>
	 * https://en.wikipedia.org/wiki/Tf%E2%80%93idf#Inverse_document_frequency
	 * <br/>
	 * This uses the 
	 * probabilistic inverse document frequency variant: log((N-nt)/nt)
	 * @return
	 */
	public Double idf() {
		return Math.log(((double) corpus.countCorpusDocuments())/this.countDocumentsWithTerm());
	}

	public Set<Document> getDocumentsUsing() {
		return documentsUsing;
	}
	
}