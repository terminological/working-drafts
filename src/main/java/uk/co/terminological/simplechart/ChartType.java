package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE("xyline.ftl");

	String templateFilename;
	
	ChartType(String templateFilename) {
		this.templateFilename = templateFilename;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

}
