package uk.co.terminological.deid;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.FluentList;

public class CoNLL_U extends FluentList<CoNLL_U.Line> {

 	public Iterator<List<CoNLL_U.Line>> getSentences() {
 		
 		return new Iterator<List<CoNLL_U.Line>>() {
 			
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public List<Line> next() {
				// TODO Auto-generated method stub
				return null;
			}
 			
 		};
 	}
	
	
	public static abstract class Line {}
	
	public static class BlankLine extends Line {
		public String toString() {return "\n";}
	}
	
	public static class Comment extends Line {
		String text;
		public String toString() {return "# "+text+"\n";}
	}
	
	public static class Entry  extends Line {
		
		/*
ID: Word index, integer starting at 1 for each new sentence; may be a range for multiword tokens; may be a decimal number for empty nodes.
FORM: Word form or punctuation symbol.
LEMMA: Lemma or stem of word form.
UPOS: Universal part-of-speech tag.
XPOS: Language-specific part-of-speech tag; underscore if not available.
FEATS: List of morphological features from the universal feature inventory or from a defined language-specific extension; underscore if not available.
HEAD: Head of the current word, which is either a value of ID or zero (0).
DEPREL: Universal dependency relation to the HEAD (root iff HEAD = 0) or a defined language-specific subtype of one.
DEPS: Enhanced dependency graph in the form of a list of head-deprel pairs.
MISC: Any other annotation.
		 */
		
		String ID;
		String FORM;
		String LEMMA = "_";
		String UPOS = "_";
		String XPOS = "_";
		String FEATS = "_";
		String HEAD = "_";
		String DEPREL = "_";
		String DEPS = "_";
		String MISC = "_";
		
		public String toString() {return ID+"\t"+FORM+"\t"+LEMMA+"\t"+UPOS+"\t"+XPOS+"\t"+FEATS+"\t"+HEAD+"\t"+DEPREL+"\t"+DEPS+"\t"+MISC+"\n";}
		
	}
	
	
}
