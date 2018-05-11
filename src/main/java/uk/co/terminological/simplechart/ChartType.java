package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE("xyplot.ftl"),
	XY_MULTI_LINE("xymultiplot.ftl");

	String templateFilename;
	
	ChartType(String templateFilename) {
		this.templateFilename = templateFilename;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

}
