package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE("xyplot.ftl", GnuplotWriter.class),
	XY_MULTI_LINE("xymultiplot.ftl",GnuplotWriter.class), 
	XY_SCATTER("xyscatter.ftl",GnuplotWriter.class),
	XYZ_CONTOUR("xyzcontour.ftl",GnuplotWriter.class), 
	NETWORK("d3Network.ftl",D3JSWriter.Nodes.class);

	String templateFilename;
	
	@SuppressWarnings("unchecked")
	ChartType(String templateFilename, @SuppressWarnings("rawtypes") Class<? extends Writer> writer) {
		this.templateFilename = templateFilename;
		this.writer = (Class<? extends Writer>) writer;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

	Class<? extends Writer> writer;
	
	
	@SuppressWarnings("unchecked")
	public <X> Class<? extends Writer> getWriter() {
		return (Class<? extends Writer>) writer;
	}

}
