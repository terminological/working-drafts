package uk.co.terminological.deid;

import uk.co.terminological.datatypes.FluentList;

//TODO: proper java pojo. better interoperability with BRATFormat.
public class CommonFormat {

	public static class Records extends FluentList<Record> {}
	
	public static class Record {
		String id;
		String documentText;
		Spans spans = new Spans();
	}
	
	public static class Spans extends FluentList<Span> {}
	
	public static class Span implements Comparable<Span> {
		Integer start;
		Integer end;
		String type;
		String subtype;
		
		public static Span from(Integer start, Integer end, String type, String subtype) {
			Span out = new Span(); out.start = start; out.end = end; out.type = type; out.subtype = subtype; return out;
		}

		@Override
		public int compareTo(Span that) {
			return this.start.compareTo(that.start) != 0 ?
					this.start.compareTo(that.start) :
						this.end.compareTo(that.end);
		}
		
		public boolean intersects(Integer start, Integer end) {
			return (this.start <= start && this.end >= start) ||
					(this.start <= end && this.end >= end);
		}
		
		public boolean after(Integer end) {
			return (this.start > end);
		}
		
		public boolean before(Integer start) {
			return (this.end < start);
		}

		public boolean isType(String string) {
			return type.equals(string);
		}
	}
}
