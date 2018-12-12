package uk.co.terminological.simplechart;

import java.io.File;

public enum ChartType {
	XY_LINE("xyplot.ftl", GnuplotWriter.class),
	XY_MULTI_LINE("xymultiplot.ftl",GnuplotWriter.class), 
	XY_SCATTER("xyscatter.ftl",GnuplotWriter.class),
	XYZ_CONTOUR("xyzcontour.ftl",GnuplotWriter.class);

	String templateFilename;
	
	ChartType(String templateFilename, Class<? extends Writer<?>> writer) {
		this.templateFilename = templateFilename;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

	Class<? extends Writer<?>> writer;
	
	
	public Class<? extends Writer<?>> getWriter() {
		return writer;
	}

}
