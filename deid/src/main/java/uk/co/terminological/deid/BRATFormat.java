package uk.co.terminological.deid;

import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.StringUtils;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.FluentMap;

/*
 * http://brat.nlplab.org/standoff.html
 */
//TODO: Brat format reader. Need to be able to input new annotations at a minimum and get those
// to interoperate with StanfordNLP training formats
//TODO: Brat configuration file writers. http://brat.nlplab.org/configuration.html.
public class BRATFormat {

	String documentText;
	FluentList<Annotation> standoffAnnotations = FluentList.empty();
	String id;
	
	transient FluentMap<Class<? extends Annotation>, Integer> counts = FluentMap.empty();
	transient Map<Class<? extends Annotation>,String> prefixes = new FluentMap<Class<? extends Annotation>,String>()
			.and(TextBoundAnnotation.class, "T")
			.and(EventAnnotation.class, "E")
			.and(RelationAnnotation.class, "R")
			.and(EquivalenceAnnotation.class, "*")
			.and(AttributeAnnotation.class, "M")
			.and(NormalisedAnnotation.class, "N")
			.and(NoteAnnotation.class, "#");
	
	public static BRATFormat create(String text, String id) {
		BRATFormat out = new BRATFormat();
		out.documentText = text;
		out.id = id;
		return out;
	}
	
	public String getId() {return id;}
	public String getDocumentText() {return documentText;}
	
	public String getStandoffOutput() {
		StringBuilder out = new StringBuilder();
		standoffAnnotations.forEach(s->out.append(s.toString()+System.lineSeparator()));
		return out.toString();
	}
	
	
	public BRATFormat withAnnotation(Annotation ann) {
		standoffAnnotations.add(assignId(ann));
		return this;
	}
	
	private <X extends Annotation> X assignId(X ann) {
		Integer tmp = counts.get(ann.getClass());
		
		tmp = (tmp == null ? 1 : tmp);
		ann.id = prefixes.get(ann.getClass())+tmp;
		counts.put(ann.getClass(), tmp+1);
		return ann;
	}
	
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
		
		public static TextBoundAnnotation textBound(String type,int start,int end,String text) {
			TextBoundAnnotation out = new TextBoundAnnotation();
			out.type = type;
			out.start.add(start);
			out.end.add(end);
			out.text = text;
			return out;
		}
		
		public static EventAnnotation event(String role, TextBoundAnnotation trigger) {
			EventAnnotation out = new EventAnnotation();
			out.role.add(role);
			out.triggerId.add(trigger.id);
			return out;
		}
		
		/*TODO:
		public static EventAnnotation event(
		public static RelationAnnotation relation(
		public static EquivalenceAnnotation equivalence(
		public static AttributeAnnotation attribute(
		public static NormalisedAnnotation normalisation(
		*/
		
		public static NoteAnnotation note(String type, Annotation ann, String comment) {
			NoteAnnotation out = new NoteAnnotation();
			out.type = type;
			out.targetId = ann.id;
			out.comment = comment;
			return out;
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
		
		public EventAnnotation with(String role, TextBoundAnnotation trigger) {
			this.role.add(role);
			this.triggerId.add(trigger.id);
			return this;
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
