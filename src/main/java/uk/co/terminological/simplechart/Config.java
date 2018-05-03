package uk.co.terminological.simplechart;

import java.io.IOException;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Config {

	Chart<?> chart;
	String title;
	String xLabel;
	String yLabel;
	OutputTarget target = OutputTarget.SCREEN;

	
	// ====== Getters =======
	
	public OutputTarget getOutputTarget() {
		return target;
	}

	public String getOutputFile() {
		return chart.getFile(target.getFileType()).getAbsolutePath();
	}

	public String getYLabel() {
		return yLabel;
	}

	public String getTitle() {
		return title;
	}
	
	public String getXLabel() {
		return xLabel;
	}

	// ======= Freemarker accessories ======
	
	public int indexOf(String dim) {
		int i=1;
		for (Tuple<Dimension, ?> binding: chart.bindings) {
			if (binding.getFirst().equals(Chart.Dimension.valueOf(dim))) return i;
			i++;
		}
		return -1;
	}
	
	public boolean hasDimension(String dim) {
		return indexOf(dim) != -1;
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

	public Config withOutputTarget(OutputTarget target) {
		this.target = target;
		return this;
	}
	
	public Chart<?> done() {
		return chart;
	}
	
	public void render() throws IOException, TemplateException {
		chart.render();
	}
}