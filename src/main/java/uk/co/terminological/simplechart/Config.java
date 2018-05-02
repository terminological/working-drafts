package uk.co.terminological.simplechart;

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

	
	// ====== Fluent Builders =======
	
	private Config() {
	}

	public static Config create(Chart<?> chart, String title) {
		Config out = new Config();
		out.chart = chart;
		out.title = title;
		return out;
	}

	Config withChart(Chart<?> chart) {
		this.chart = chart;
		this.chart.config = this;
		return this;
	}

	Config withTitle(String title) {
		this.title = title;
		return this;
	}

	Config withXLabel(String xLabel) {
		this.xLabel = xLabel;
		return this;
	}

	Config withYLabel(String yLabel) {
		this.yLabel = yLabel;
		return this;
	}

	Config withOutputTarget(OutputTarget target) {
		this.target = target;
		return this;
	}
	
	public Chart<?> done() {
		return chart;
	}
	
}