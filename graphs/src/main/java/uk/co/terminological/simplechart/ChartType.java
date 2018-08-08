package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE("xyplot.ftl"),
	XY_MULTI_LINE("xymultiplot.ftl"), 
	XY_SCATTER("xyscatter.ftl"),
	XYZ_CONTOUR("xyzcontour.ftl");

	String templateFilename;
	
	ChartType(String templateFilename) {
		this.templateFilename = templateFilename;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

}
