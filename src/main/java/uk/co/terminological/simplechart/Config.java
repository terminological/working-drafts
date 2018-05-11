package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Config {

	Chart<?> chart;
	String title;
	String xLabel;
	String yLabel;
	String xScale = "auto";
	String yScale = "auto";
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
		return xLabel;
	}
	
	public String getYLabel() {
		return yLabel;
	}

	public String getXScale() {
		return xScale == null ? "auto" : xScale;
	}
	
	public String getYScale() {
		return yScale == null ? "auto" : yScale;
	}

	
	public List<String> getCustomCommands() {
		return customCommands;
	}

	// ======= Freemarker accessories ======
	
	public List<Integer> indexesOf(String dim) {
		List<Integer> out = new ArrayList<>();
		int i=1;
		for (Triple<Dimension, ?, String> binding: chart.bindings) {
			if (binding.getFirst().equals(Chart.Dimension.valueOf(dim))) out.add(i);
			i++;
		}
		return out;
	}
	
	public int indexOf(String dim) {
		int i=1;
		for (Triple<Dimension, ?, String> binding: chart.bindings) {
			if (binding.getFirst().equals(Chart.Dimension.valueOf(dim))) return i;
			i++;
		}
		return -1;
	}
	
	public boolean hasDimension(String dim) {
		return indexOf(dim) != -1;
	}
	
	public String getLabelForPlot(int i) {
		return chart.bindings.get(i).getThird();
	}
	
	// ====== Fluent Builders =======
	
	private Config() {
	}

	public static Config create(Chart<?> chart, String title) {
		Config out = new Config();
		out.chart = chart;
		out.title = title;
		return out;
	}

	public Config withChart(Chart<?> chart) {
		this.chart = chart;
		this.chart.config = this;
		return this;
	}

	public Config withTitle(String title) {
		this.title = title;
		return this;
	}

	public Config withXLabel(String xLabel) {
		this.xLabel = xLabel;
		return this;
	}

	public Config withYLabel(String yLabel) {
		this.yLabel = yLabel;
		return this;
	}
	
	public Config withXScale(float start, float end) {
		this.xScale = "["+StringUtils.joinWith(":", Float.toString(start),Float.toString(end))+"]";
		return this;
	}

	public Config withYScale(float start, float end) {
		this.yScale = "["+StringUtils.joinWith(":", Float.toString(start),Float.toString(end))+"]";
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
	
	public Chart<?> done() {
		return chart;
	}
	
	public void render() throws IOException, TemplateException {
		chart.render();
	}
}