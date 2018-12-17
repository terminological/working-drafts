package uk.co.terminological.simplechart;

public enum ChartType {
	XY_LINE("xyplot.ftl", GnuplotWriter.class),
	XY_MULTI_LINE("xymultiplot.ftl",GnuplotWriter.class), 
	XY_SCATTER("xyscatter.ftl",GnuplotWriter.class),
	XYZ_CONTOUR("xyzcontour.ftl",GnuplotWriter.class), 
	NETWORK("d3Network.ftl",D3JSWriter.Nodes.class), 
	CHORD("d3Chord.ftl",D3JSWriter.Matrix.class),
	WORDCLOUD(null,WordcloudWriter.class), 
	XYBAR("ggplot.ftl",GgplotWriter.BarChart.class),
	;

	String templateFilename;
	
	ChartType(String templateFilename, Class<? extends Writer> writer) {
		this.templateFilename = templateFilename;
		this.writer = (Class<? extends Writer>) writer;
	};
	
	public String getTemplateFilename() {
		return templateFilename;
	}

	Class<? extends Writer> writer;
	

	public <X> Class<? extends Writer> getWriter() {
		return (Class<? extends Writer>) writer;
	}

}
