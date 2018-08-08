package uk.co.terminological.deid;

import uk.co.terminological.datatypes.FluentList;

public class CoNLL_U extends FluentList<Line> {

 	
	
	
	public static abstract class Line {}
	
	public static class BlankLine extends Line {}
	
	public static class Comment extends Line {}
	
	public static class Entry  extends Line {
		
	}
	
	
}
