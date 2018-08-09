package uk.co.terminological.deid;

import java.util.List;

import edu.stanford.nlp.util.StringUtils;

/*
 * http://brat.nlplab.org/standoff.html
 */
public class BRATFormat {

	List<Annotation> standoffs;
	
	public static abstract class Annotation {
		String id;
		
		<X,Y> String join(X[] k, Y[] v, String sep, String sep2) {
			StringBuilder tmp = new StringBuilder();
			for (int i=0; i<k.length; i++) {
				if (tmp.length() !=0) tmp.append(sep2);
				tmp.append(k[i].toString()+sep+v[i].toString());
			}
			return tmp.toString();
		}
	}
	
	
	public static class TextBoundAnnotation extends Annotation {
		String type;
		Integer start[];
		Integer end[];
		String text;
		
		public String toString() {
			return id+"\t"+type+" "+join(start,end," ",";")+"\t"+text;
		}
		
	}
	public static class EventAnnotation extends Annotation {
		String role[];
		String triggerId[];
		
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
		String[] equivIds;
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
	// public static class NormalisedAnnotation extends Annotation {}
	public static class NoteAnnotation extends Annotation {
		String type;
		String targetId;
		String comment;
		public String toString() {
			return id+"\t"+type+" "+targetId+"\t"+comment;
		}
	}
	
}
