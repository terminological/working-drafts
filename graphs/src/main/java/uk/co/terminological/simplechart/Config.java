package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.FluentMap;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Config {

	Chart chart;
	String title;
	Map<Dimension,String> labels = FluentMap.with(Dimension.X, "x").and(Dimension.Y, "y");
	Tuple<Double,Double> xScale;
	Tuple<Double,Double> yScale;
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
	
	public String getXmin() {
		if (yScale == null) return null;
		return Double.toString(xScale.getFirst());
	}
	
	public String getYmin() {
		if (yScale == null) return null;
		return Double.toString(yScale.getFirst());
	}
	
	public String getXmax() {
		if (xScale == null) return null;
		return Double.toString(xScale.getSecond());
	}
	
	public String getYmax() {
		if (xScale == null) return null;
		return Double.toString(xScale.getSecond());
	}
	
	public String getXScale() {
		if (xScale == null) return null;
		return "["+StringUtils.joinWith(":", Double.toString(xScale.getFirst()),Double.toString(xScale.getSecond()))+"]";
	}
	
	public String getYScale() {
		if (yScale == null) return null;
		return "["+StringUtils.joinWith(":", Double.toString(yScale.getFirst()),Double.toString(yScale.getSecond()))+"]";
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
		this.xScale = Tuple.create(start, end);
		return this;
	}

	public Config withYScale(double start, double end) {
		this.yScale = Tuple.create(start, end);
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
}