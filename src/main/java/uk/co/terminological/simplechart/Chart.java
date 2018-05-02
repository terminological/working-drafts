package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	List<X> data;
	Template template;
	Figure figure;
	Config config;
	List<Tuple<Dimension,Function<X,Object>>> bindings = new ArrayList<>();
	Map<String,String> customField = new HashMap<>();
	String filename;
	
	public static Logger log = LoggerFactory.getLogger(Chart.class);
	
	protected Chart(List<X> data, String title, Template template, File workingDirectory, Figure figure) {
		this.data = data;
		this.template = template;
		this.figure = figure;
		
		this.config = Config.create(this, title);
		log.info("Chart at: directory="+workingDirectory+"; file="+filename);
	}
	
	
	
	public Chart<X> bind(Dimension dimension, Function<X,Object> binding) {
		bindings.add(Tuple.create(dimension, binding));
		return this;
	};
	
	public Chart<X> withConfig(Config config) {
		this.config = config;
		this.config.chart = this;
		return this;
	}
	
	public Chart<X> withCustomField(String key, Object value) {
		if (Arrays.asList("data","config").contains(key)) throw new RuntimeException("Cannot use the custom key: "+key);
		this.customField.put(key, value.toString());
		return this;
	}
	
	public Config config() {return config;}
	
	public void render() throws IOException, TemplateException {
		Writer.write(this, type);
	}
	
	public void render(File template) throws IOException, TemplateException {
		Writer.write(this, template);
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL,Y_LOW,Y_HIGH
	}
	
	public File getWorkingDirectory() {
		return this.workingDirectory;
	}
	
	public File getFile(String extension) {return new File(getWorkingDirectory(),filename+"."+extension);}
}
