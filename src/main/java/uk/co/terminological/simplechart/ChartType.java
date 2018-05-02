package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE();

	String templateFilename;
	
	ChartType(String templateFilename) {
		this.templateFilename = templateFilename;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

}
