package uk.co.terminological.deid;

import java.io.IOException;
import java.io.Writer;

import uk.co.terminological.datatypes.FluentList;

public interface CoNLL_U  {
	
	public CoNLL_U addComment(String comment);
	public CoNLL_U addBlank();
	public CoNLL_U addEntry(Entry entry);

	public static class List extends FluentList<CoNLL_U.Line> implements CoNLL_U {
	
	public CoNLL_U.List addComment(String comment) {
		Comment toAdd = new Comment();
		toAdd.text = comment;
		this.add(toAdd);
		return this;
	}
	
	public CoNLL_U.List addBlank() {
		BlankLine toAdd = new BlankLine();
		this.add(toAdd);
		return this;
	}
	
	public CoNLL_U.List addEntry(Entry toAdd) {
		this.add(toAdd);
		return this;
	}
 	
	}
	
	public static class Printer implements CoNLL_U {
	
		Writer writer;
		public Printer(Writer writer) {
			this.writer = writer;
		}
		@Override
		public CoNLL_U addComment(String comment) {
			Comment toAdd = new Comment();
			toAdd.text = comment;
			try {
				writer.write(toAdd.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		@Override
		public CoNLL_U addBlank() {
			BlankLine toAdd = new BlankLine();
			try {
				writer.write(toAdd.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		@Override
		public CoNLL_U addEntry(Entry entry) {
			try {
				writer.write(entry.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		
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
		
		public static Entry create(String ID, String FORM, String LEMMA, String UPOS) {
			Entry toAdd = new Entry();
			toAdd.ID = ID;
			toAdd.FORM = FORM;
			toAdd.LEMMA = LEMMA != null ? LEMMA : "_";
			toAdd.UPOS = UPOS != null ? UPOS : "_";
			return toAdd;
		}
		
		public Entry withMisc(String key, String value) {
			if (MISC != "_") {
				MISC = MISC+"|";
			} else {
				MISC = "";
			}
			MISC = MISC + key+"="+value;
			return this;
		}
	}
	
	
}
