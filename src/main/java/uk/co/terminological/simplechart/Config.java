package uk.co.terminological.simplechart;

public class Config {
	
	Chart<?> chart;
	
	private Config() {}
	
	public static Config create(Chart<?> chart) {
		Config out = new Config();
		out.chart =chart;
		return out;
	}
	
	Config withChart(Chart<?> chart) {
		this.chart = chart;
		this.chart.config = this;
		return this;
	}
	
	String title; Config withTitle(String title) {this.title = title; return this; } public String getTitle() {return title;}
	String xLabel; Config withXLabel(String xLabel) {this.xLabel = xLabel; return this; } public String getXLabel() {return xLabel;}
	String yLabel; Config withYLabel(String yLabel) {this.yLabel = yLabel; return this; } public String getYLabel() {return yLabel;}
	OutputTarget target = OutputTarget.SCREEN; Config withOutputTarget(OutputTarget target) {this.target = target; return this;} public OutputTarget getOutputTarget() {return target;}
	
	public String getOutputFile() {
		return chart.getFile(target.getFileType()).getAbsolutePath();
	}
	
	public Chart<?> done() {return chart;}
}