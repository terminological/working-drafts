package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Config {

	Chart chart;
	String title;
	Map<Dimension,String> labels = FluentMap.with(Dimension.X, "x").and(Dimension.Y, "y");
	Map<Dimension,Tuple<Double,Double>> scales = FluentMap.create();
	OutputTarget target = OutputTarget.SCREEN;
	List<String> customCommands = new ArrayList<>();

	
	// ====== Getters =======
	
	public OutputTarget getOutputTarget() {
		return target;
	}

	public String getOutputFile() {
		return chart.getFile(target.getFileType()).getAbsolutePath();
	}

	public String getTitle() {
		return title;
	}
	
	public String getXLabel() {
		return labels.get(Dimension.X);
	}
	
	public String getYLabel() {
		return labels.get(Dimension.Y);
	}

	public String getLabel(String dimension) {
		return labels.get(Dimension.valueOf(dimension));
	}

	public String getMin(String dimension) {
		return getScaleValue(Dimension.valueOf(dimension),true);
	}
	
	public String getMax(String dimension) {
		return getScaleValue(Dimension.valueOf(dimension),false);
	}
	
	private String getScaleValue(Dimension dimension, boolean min) {
		if (!scales.containsKey(Dimension.X)) return null;
		if (min) {
			return Double.toString(scales.get(Dimension.X).getFirst());
		} else {
			return Double.toString(scales.get(Dimension.X).getSecond());
		}
	}
	
	public String getXmin() {return getScaleValue(Dimension.X,true);}
	public String getYmin() {return getScaleValue(Dimension.X,true);}
	public String getXmax() {return getScaleValue(Dimension.X,false);}
	public String getYmax() {return getScaleValue(Dimension.X,false);}
	
	public String getXScale() {
		if (!scales.containsKey(Dimension.X)) return null;
		return "["+getXmin()+":"+getXmax()+"]";
	}
	
	public String getYScale() {
		if (!scales.containsKey(Dimension.Y)) return null;
		return "["+getYmin()+":"+getYmax()+"]";
	}

	public List<String> getCustomCommands() {
		return customCommands;
	}

	// ====== Specific extension points for supporting freemarker templates
	
	public List<Series<?>> getSeries() {
		return chart.getSeries();
	}
	
	// ====== Fluent Builders =======
	
	private Config() {
	}

	public static Config create(Chart chart, String title) {
		Config out = new Config();
		out.chart = chart;
		out.title = title;
		return out;
	}

	public Config withChart(Chart chart) {
		this.chart = chart;
		this.chart.config = this;
		return this;
	}

	public Config withTitle(String title) {
		this.title = title;
		return this;
	}

	public Config withLabel(Dimension dim, String label) {
		this.labels.put(dim, label);
		return this;
	}
	
	public Config withXLabel(String xLabel) {
		return withLabel(Dimension.X, xLabel);
	}

	public Config withYLabel(String yLabel) {
		return withLabel(Dimension.Y, yLabel);
	}
	
	public Config withXScale(double start, double end) {
		this.scales.put(Dimension.X,Tuple.create(start, end));
		return this;
	}

	public Config withYScale(double start, double end) {
		this.scales.put(Dimension.Y,Tuple.create(start, end));
		return this;
	}
	
	public Config withOutputTarget(OutputTarget target) {
		this.target = target;
		return this;
	}
	
	public Config withCustomCommand(String command) {
		this.customCommands.add(command);
		return this;
	}
	
	public Chart done() {
		return chart;
	}
	
	public void render() throws IOException, TemplateException {
		chart.render();
	}

	public String getLabel(Dimension dimension) {
		return labels.getOrDefault(dimension, dimension.toString());
	}

	public Config withScale(Dimension z, double f, double g) {
		scales.put(z, Tuple.create(f, g));
		return this;
	}
	
	public List<String> getGGplotLimits() {
		return scales.entrySet().stream().map(kv -> { 
			return kv.getKey().name().toLowerCase()+"=c("+
					kv.getValue().getFirst()+","+
					kv.getValue().getSecond()+")";
		}).collect(Collectors.toList());
	}
	
	public List<String> getGGplotLabels() {
		return labels.entrySet().stream().map(kv -> { 
			return kv.getKey().name().toLowerCase()+"="+
					"\""+kv.getValue()+"\"";
		}).collect(Collectors.toList());
	}
}