package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateNotFoundException;
import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	List<X> data;
	Config config = new Config();
	List<Tuple<Dimension,Function<X,Object>>> bindings = new ArrayList<>();
	Map<String,String> customField = new HashMap<>();
	
	public Chart(List<X> data) {
		this.data = data;
	}
	
	public static <X> Chart<X> create(List<X> data) {
		return new Chart<>(data);
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
		X,Y,Z,COLOUR,SIZE,LABEL
	}
	
	public static class Config {
		
		private Chart<?> chart;
		Config withChart(Chart<?> chart) {
			this.chart = chart;
			this.chart.config = this;
			return this;
		}
		
		String title; Config withTitle(String title) {this.title = title; return this; }
		String xLabel; Config withXLabel(String xLabel) {this.xLabel = xLabel; return this; }
		String yLabel; Config withYLabel(String yLabel) {this.yLabel = yLabel; return this; }
	}
	
}
