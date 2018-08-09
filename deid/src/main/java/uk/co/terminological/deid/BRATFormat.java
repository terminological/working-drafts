package uk.co.terminological.deid;

import java.util.List;

import edu.stanford.nlp.util.StringUtils;
import uk.co.terminological.datatypes.FluentList;

/*
 * http://brat.nlplab.org/standoff.html
 */
public class BRATFormat {

	String documentText;
	FluentList<Annotation> standoffAnnotations = FluentList.empty();
	
	
	
	
	public static abstract class Annotation {
		String id;
		
		<X,Y> String join(List<X> k, List<Y> v, String sep, String sep2) {
			StringBuilder tmp = new StringBuilder();
			for (int i=0; i<k.size(); i++) {
				if (tmp.length() !=0) tmp.append(sep2);
				tmp.append(k.get(i).toString()+sep+v.get(i).toString());
			}
			return tmp.toString();
		}
	}
	
	public static class TextBoundAnnotation extends Annotation {
		String type;
		FluentList<Integer> start = FluentList.empty();
		FluentList<Integer> end = FluentList.empty();
		String text;
		
		public String toString() {
			return id+"\t"+type+" "+join(start,end," ",";")+"\t"+text;
		}
		
	}
	public static class EventAnnotation extends Annotation {
		FluentList<String> role = FluentList.empty();
		FluentList<String> triggerId = FluentList.empty();
		
		public String toString() {
			return id+"\t"+join(role,triggerId,":"," ");
		}
	}
	public static class RelationAnnotation extends Annotation {
		String type;
		String sourceId;
		String targetId;
		public String toString() {
			return id+"\tArg1:"+sourceId+" Arg2:"+targetId;
		}
	}
	public static class EquivalenceAnnotation extends Annotation {
		FluentList<String> equivIds = FluentList.empty();
		public String toString() {
			return "*\t"+StringUtils.join(equivIds," ");
		}
	}
	public static class AttributeAnnotation extends Annotation {
		String attribute;
		String targetId;
		String value = "";
		public String toString() {
			return id+"\t"+attribute+" "+targetId+(value.isEmpty()?"":" "+value);
		}
	}
	public static class NormalisedAnnotation extends Annotation {
		String targetId;
		String RID; //e.g.Wikipedia / SNOMED. defined in tools.conf  
		String EID; //identifier in that data set
		String text;
		public String toString() {
			return id+"\tReference "+targetId+" "+RID+":"+EID+"\t"+text;
		}
	}
	public static class NoteAnnotation extends Annotation {
		String type;
		String targetId;
		String comment;
		public String toString() {
			return id+"\t"+type+" "+targetId+"\t"+comment;
		}
	}
	
}
