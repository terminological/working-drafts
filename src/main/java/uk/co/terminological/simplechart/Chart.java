package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	List<X> data;
	Config config = new Config();
	List<Tuple<Dimension,Function<X,Object>>> bindings = new ArrayList<>();
	Map<String,String> customField = new HashMap<>();
	File workingDirectory;
	String filename;
	
	public static Logger log = LoggerFactory.getLogger(Chart.class);
	
	public Chart(List<X> data, File workingDirectory, String filename) {
		this.data = data;
		this.workingDirectory = workingDirectory;
		this.filename = filename;
		log.info("Chart at: directory="+workingDirectory+"; file="+filename);
	}
	
	public static <X> Chart<X> create(List<X> data) {
		try {
			File tmp = File.createTempFile("gnuplotTmp","");
			tmp.mkdirs();
			return new Chart<>(data,tmp,Long.toString(System.nanoTime()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <X> Chart<X> create(List<X> data, File outputDirectory, String filename) {
		return new Chart<X>(data,outputDirectory,filename);
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
	
	public void render(ChartType type) throws IOException {
		Writer.write(this, type);
	}
	
	public void render(File template) throws IOException {
		Writer.write(this, template);
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL,Y_LOW,Y_HIGH
	}
	
	public static class Config {
		
		private Chart<?> chart;
		Config withChart(Chart<?> chart) {
			this.chart = chart;
			this.chart.config = this;
			return this;
		}
		
		String title; Config withTitle(String title) {this.title = title; return this; } String getTitle() {return title;}
		String xLabel; Config withXLabel(String xLabel) {this.xLabel = xLabel; return this; } String getXLabel() {return xLabel;}
		String yLabel; Config withYLabel(String yLabel) {this.yLabel = yLabel; return this; } String getYLabel() {return yLabel;}
	}

	public File getWorkingDirectory() {
		return this.workingDirectory;
	}
	
	public File getFile(String extension) {return new File(getWorkingDirectory(),filename+"."+extension);}
}
