package uk.co.terminological.pipestream;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.datatypes.TupleList;



public class ExecutionHistoryUtils implements EventBusAware {

	Path dir;
	Configuration cfg;
	Logger log = LoggerFactory.getLogger(ExecutionHistoryUtils.class);

	public static final String BIG_DOT_GRAPH = "fullDot.ftl";
	public static final String SMALL_DOT_GRAPH = "smallDot.ftl";
	public static final String TSV = "tsv.ftl";

	public ExecutionHistoryUtils(Path dir) {
		this.dir = dir;
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
		cfg.setClassForTemplateLoading(ExecutionHistoryUtils.class, "/freemarker");

	}


	public void generate(String resourceName, Path outfile, String name) {
		try {

			Template template = cfg.getTemplate(resourceName);
			Map<String,Object> root = new HashMap<String,Object>();
			root.put("model", new Model(this.getEventBus().getEventHistory(), this.getEventBus().getProcessingHistory()));
			root.put("name",name);
			PrintWriter out = new PrintWriter(Files.newOutputStream(outfile));
			template.process(root, out);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void executeGraphviz(Path dotFile) throws IOException {
		log.info("Starting Graphviz...");
		Process process2 = new ProcessBuilder("/usr/bin/dot","-O","-Tpng",dotFile.toString())
				.redirectOutput(Redirect.INHERIT)
				.start();

		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		log.info("Ending Graphviz...");
	}

	//TODO: This just about works but there are issues
	//firstly there is a mix up between the ideas of an instance of the processor (usually 1)
	//versus the instance of a message which means that we may want to be able to 
	//display instances of processors and counts of messages. 
	//I think this might be better by just putting everything into a flat list and
	//doing SQL like operations on it.
	
	//This distinction is not well made throughout this framework and there is no separation between'
	//the concepts of procesor name and message name... This could be achieved with the subtypes of metadata.
	
	
	public static class Model {

		List<Pair> produced = new ArrayList<>();
		List<Pair> consumed = new ArrayList<>();

		public Model(
				TupleList<? extends Metadata, ? extends Metadata> produced,
				TupleList<? extends Metadata, ? extends Metadata> consumed) {
			produced.forEach(
					tuple -> this.produced.add(new Pair(tuple))
					);
			consumed.forEach(
					tuple -> this.consumed.add(new Pair(tuple))
					);
		}

		public List<Pair> getProduced() {
			return produced;
		}

		public List<Pair> getConsumed() {
			return consumed;
		}
		
		public List<Pair> getAll() {
			return FluentList.create(produced,consumed);
		}
 		
		public Collection<Entry> getUniqueEntryById() {
			HashMap<String,Entry> tmp = new HashMap<>();
			getAll().forEach(p -> {
				tmp.put(p.getSource().getId(),p.getSource());
				tmp.put(p.getTarget().getId(),p.getTarget());
			});
			return tmp.values();
		}
		
		public Collection<Entry> getUniqueEntryByType() {
			HashMap<String,Entry> tmp = new HashMap<>();
			getAll().forEach(p -> {
				tmp.put(p.getSource().getTypeId(),p.getSource());
				tmp.put(p.getTarget().getTypeId(),p.getTarget());
			});
			return tmp.values();
		}
		
		/**
		 * uses custom hashmap to aggregate pairs that match.
		 * @return
		 */
		public Map<Pair,Integer> getUniquePairAndCount() {
			Map<String,Integer> counts = new HashMap<>();
			Map<String,Pair> out = new HashMap<>();
			getAll().forEach(p -> {
				Integer tmp = counts.get(p.getTypeRelationship());
				if (tmp == null) tmp=0;
				counts.put(p.getTypeRelationship(), tmp+1);
				out.put(p.getTypeRelationship(), p);
			});
			Map<Pair,Integer> out2 = new HashMap<>();
			out.entrySet().forEach(kv -> {
				out2.put(kv.getValue(), counts.get(kv.getKey()));
			});
			return out2;
		}

		

		public static class Pair {

			public String getTypeRelationship() {
				return getSource().getTypeId()+" -> "+getTarget().getTypeId();
			}

			public String getRelationship() {
				return getSource().getId()+" -> "+getTarget().getId();
			}
			
			Entry source; Entry target;

			Pair(Tuple<? extends Metadata, ? extends Metadata> t) {
				source = new Entry(t.getFirst());
				target = new Entry(t.getSecond());
			}

			public Entry getSource() {return source;}
			public Entry getTarget() {return target;}



		}

		public static class Entry {

			Metadata m;
			Entry(Metadata metadata) {
				m = metadata;
			}

			public String getId() {return "instance_"+Integer.toHexString(m.hashCode());}
			public String getTypeId() {return "type_"+Integer.toHexString(m.typeDescription.hashCode());}
			public String getName() {return m.name;}
			public String getType() {return m.typeDescription;}

			
		}

	}

}


