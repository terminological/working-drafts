package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	List<X> data;
	Template template;
	Figure figure;
	Config config;
	List<Triple<Dimension,Function<X,Object>,String>> bindings = new ArrayList<>();
	Map<String,String> customField = new HashMap<>();
	String filename;
	Class<? extends Writer<X>> writerCls;
	
	public static Logger log = LoggerFactory.getLogger(Chart.class);
	
	protected Chart(List<X> data, String title, Template template, Class<? extends Writer<X>> class1, File workingDirectory, Figure figure) {
		this.data = data;
		this.template = template;
		this.figure = figure;
		this.filename = title.replaceAll("[^a-zA-Z0-9]+", "_");
		this.writerCls = class1;
		this.config = Config.create(this, title);
		log.info("Chart at: directory="+workingDirectory+"; file="+filename);
	}
	
	public Chart<X> bind(Dimension dimension, Function<X,Object> binding) {
		bindings.add(Triple.create(dimension, binding,""));
		return this;
	};
	
	public Chart<X> bind(Dimension dimension, Function<X,Object> binding, String label) {
		bindings.add(Triple.create(dimension, binding,label));
		return this;
	};
	
	public Chart<X> withConfig(Config config) {
		this.config = config;
		this.config.chart = this;
		return this;
	}
	
	public Chart<X> withAxes(String x,String y) {
		this.config().withXLabel(x);
		this.config().withYLabel(y);
		return this;
	}
	
	public Chart<X> withCustomField(String key, Object value) {
		if (Arrays.asList("data","config").contains(key)) throw new RuntimeException("Cannot use the custom key: "+key);
		this.customField.put(key, value.toString());
		return this;
	}
	
	public Config config() {return config;}
	
	public void render() throws IOException, TemplateException {
		Writer<X> writer;
		try {
			writer = writerCls.getDeclaredConstructor(Chart.class).newInstance(this);
			writer.process();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL,Y_LOW,Y_HIGH,Y_FIT,
		ID,WEIGHT,SOURCE_ID,TARGET_ID
	}
	
	public File getWorkingDirectory() {
		return figure.workingDirectory;
	}
	
	public File getFile(String extension) {return new File(getWorkingDirectory(),filename+"."+extension);}
}
