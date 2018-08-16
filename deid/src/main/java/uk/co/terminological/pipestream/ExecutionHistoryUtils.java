package uk.co.terminological.pipestream;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.datatypes.TupleList;
import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.EventHandler.HandlerMetadata;



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
		Process process2 = new ProcessBuilder("/usr/bin/dot","-O -Tpng",dotFile.toString())
				.redirectOutput(Redirect.INHERIT)
				.start();

		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		log.info("Ending Graphviz...");
	}

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
		
		public Collection<Entry> getUniqueEntryById() {
			HashMap<String,Entry> tmp = new HashMap<>();
			produced.forEach(p -> {
				tmp.put(p.getSource().getId(),p.getSource());
				tmp.put(p.getTarget().getId(),p.getTarget());
			});
			consumed.forEach(p -> {
				tmp.put(p.getSource().getId(),p.getSource());
				tmp.put(p.getTarget().getId(),p.getTarget());
			});
			return tmp.values();
		}
		
		public Collection<Entry> getUniqueEntryByType() {
			HashMap<String,Entry> tmp = new HashMap<>();
			produced.forEach(p -> {
				tmp.put(p.getSource().getTypeId(),p.getSource());
				tmp.put(p.getTarget().getTypeId(),p.getTarget());
			});
			consumed.forEach(p -> {
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
			Map<Pair,Integer> out = new HashMap<>();
			produced.forEach(p -> {
				Integer tmp = out.get(p);
				if (tmp == null) tmp=0;
				out.put(p, tmp+1);
			});
			consumed.forEach(p -> {
				Integer tmp = out.get(p);
				if (tmp == null) tmp=0;
				out.put(p, tmp+1);
			});
			return out;
		}

		

		public static class Pair {

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((source == null) ? 0 : source.hashCode());
				result = prime * result + ((target == null) ? 0 : target.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Pair other = (Pair) obj;
				if (source == null) {
					if (other.source != null)
						return false;
				} else if (!source.equals(other.source))
					return false;
				if (target == null) {
					if (other.target != null)
						return false;
				} else if (!target.equals(other.target))
					return false;
				return true;
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

			public String getId() {return Integer.toHexString(m.hashCode());}
			public String getTypeId() {return Integer.toHexString(this.hashCode());}
			public String getName() {return m.name;}
			public String getType() {return m.typeDescription;}

			@Override
			public int hashCode() {
				return new HashCodeBuilder().append(m.name).append(m.typeDescription).build();
			}
			@Override
			public boolean equals(Object obj) {
				if (obj == null) { return false; }
				if (obj == this) { return true; }
				if (obj.getClass() != getClass()) {
					return false;
				}
				Entry rhs = (Entry) obj;
				return new EqualsBuilder()
						.appendSuper(super.equals(obj))
						.append(m.name, rhs.m.name)
						.append(m.typeDescription, rhs.m.typeDescription)
						.isEquals();
			}
		}

	}

}


