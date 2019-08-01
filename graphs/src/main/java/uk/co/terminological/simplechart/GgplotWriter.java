package uk.co.terminological.simplechart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang.NotImplementedException;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;

public abstract class GgplotWriter extends Writer {

	

	
	public abstract List<String> getPlots();
	
	public GgplotWriter(Chart chart) {
		super(chart);
	}
		
	@Override
	protected String extractData() {
		
		// Multiple series support
		// https://stackoverflow.com/questions/8534030/plotting-multiple-series-from-file-using-gnuplot
		// 2 blank lines - i.e. 3 newlines
		// referenced using index 0,1,2
		return getChart().getSeries().stream()
			.map(s -> extractData(s)).collect(Collectors.joining("\n\n\n"));
		
	}
	
	private <X> String extractData(Series<X> series) {
		
		Path tsv =  this.getChart().getFile("tsv").toPath();
		try {
			BufferedWriter w = Files.newBufferedWriter(tsv);
			// write headers
			w.append(series.getBindings().stream().map(t -> t.entity().name()).collect(Collectors.joining("\t"))+"\n");
			series.getData().forEach(x -> {
				String line = series.getBindings().stream()
					.map(t -> t.attribute().apply(x))
					.map(GgplotWriter::format)
					.collect(Collectors.joining("\t"));
				try {
					w.append(line+"\n");
				} catch (IOException e) {
					new RuntimeException(e);
				}
			});
			w.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		/*
		 * 
		 * Path h = this.getFile("html").toPath();
		if (!h.equals(outFile.toPath())) {
			BufferedWriter w = Files.newBufferedWriter(h);
			w.write("<html><head></head><body><img src='"+h.getParent().relativize(outFile.toPath()).toString()+"'></body></html>");
			w.close();
		}*/
		
		/*StringBuilder dfConstruct = new StringBuilder();
		StringBuilder vector = new StringBuilder();
		
		int size = series.getData().size();
		dfConstruct.append("order <- seq(0,"+(size-1)+");\n");
		dfConstruct.append("df <- data.frame(order");
		
		
		for (Triple<Chart.Dimension,Function<X,? extends Object>,String> binding: series.getBindings()) {
			String varName = binding.getFirst().name(); //+
					//(binding.getThird() == "" ? "" : "_"+binding.getThird());
			
			//Object seriesType = series.getData().stream().map(binding.getSecond()).findFirst().get();
			
			vector.append("tmp_"+varName+" <- c(");
			
			vector.append(
					series.getData().stream().map(binding.getSecond())
						.map(GgplotWriter::format)
						.collect(Collectors.joining(", "))
						);
			
			
			vector.append(");\n");
			
			dfConstruct.append(",");
			dfConstruct.append(varName+"=tmp_"+varName);
			
		}
		
		dfConstruct.append(",stringsAsFactors = FALSE);\n");
		
		return vector.toString()+"\n"+dfConstruct.toString();*/
		
		return "df <- read_csv(\""+tsv.getFileName()+"\")";
	}

	//TODO: localDate support?
	private static String format(Object o) {
		if (o == null) return "NA";
		if (o instanceof Double) {
			if (Double.isNaN((Double) o)) return "NaN";
			if (o.equals(Double.NEGATIVE_INFINITY)) return "-Inf";
			if (o.equals(Double.POSITIVE_INFINITY)) return "Inf";
		}
		if (o instanceof Number) return o.toString();
		return "'"+o.toString().replace("'", "\\'")+"'";
	}
	
	@Override
	protected Path process() throws IOException, TemplateException {
		
		getRoot().put("plots", getPlots());
		//TODO: how to control this?
		//TODO: colour schemes?
		getRoot().put("schemeName", getChart().getSeries().stream().map(s -> s.getScheme().getName()).findFirst().orElse("Set1"));
		getRoot().put("includePlotTitles", true);
		
		File outFile = getChart().getFile("png");
		//getRoot()
		Map<String,Object> wrapperRoot = new HashMap<>();
		wrapperRoot.put("output", outFile.getAbsolutePath());
		
		Template wrapper = this.getChart().getFigure().getTemplate("ggplotSingleplot.ftl");
		
		File f = getChart().getFile("R");
		PrintWriter fileOut = new PrintWriter(new FileWriter(f));
		
		StringBuilderWriter out = new StringBuilderWriter();
		getTemplate().get().process(getRoot(), out);
		out.close();
		wrapperRoot.put("plot", out.toString());
		wrapperRoot.put("title", getChart().getConfig().getTitle());
		wrapper.process(wrapperRoot, fileOut);
		fileOut.close();
		
		Chart.log.info("Starting R...");
		
		Process process2 = new ProcessBuilder("/usr/bin/R","-f",f.getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		IOUtils.copy(process2.getErrorStream(),System.out);
		
		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending R...");
		return outFile.toPath();
	}
	
	@Override
	protected String processForMultiplot(boolean includePlotTitles) throws IOException, TemplateException {
		
		getRoot().put("plots", getPlots());
		getRoot().put("includePlotTitles", includePlotTitles);
		getRoot().put("schemeName", getChart().getSeries().stream().map(s -> s.getScheme().getName()).findFirst().orElse("Set1"));
		
		StringBuilderWriter out = new StringBuilderWriter();
		getTemplate().get().process(getRoot(), out);
		out.close();
		return out.toString();
	}
	
	public static class BarChart extends GgplotWriter {

		public BarChart(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_bar(stat='identity', aes(x=X, y=Y))"					
					);
		}

		
		
	}
	
	public static class Scatter extends GgplotWriter {

		public Scatter(Chart chart) {
			super(chart);
			
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_point(stat='identity', aes(x=X, y=Y))",
					"geom_smooth(aes(x=X, y=Y), method = 'glm'"
					);
		}
	}
	
	public static class MultiPieChart extends GgplotWriter {

		public MultiPieChart(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_bar(stat='identity', aes(x=factor(X, levels=rev(X)), y=Y, fill=factor(LABEL, levels=rev(LABEL))))",
					"theme(axis.title.y=element_blank(),legend.title=element_blank())",
					"scale_fill_brewer(palette=schemeName, breaks=df$LABEL)",
					"coord_flip()"
					);
		}
		
	}
	

	public static class GroupedLineChart extends GgplotWriter {

		public GroupedLineChart(Chart chart) {
			super(chart);
		}
		
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_line(stat='identity', aes(x=X, y=Y, colour=factor(COLOUR)))",
					"scale_colour_brewer(palette=schemeName)"
				);
		}
	}

	public static class FacetedGroupedLineChart extends GgplotWriter {

		public FacetedGroupedLineChart(Chart chart) {
			super(chart);
		}
		
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_line(stat='identity', aes(x=X, y=Y, colour=factor(COLOUR)))",
					"scale_colour_brewer(palette=schemeName)",
					"facet_grid(factor(X_OUTER) ~ factor(Y_OUTER))"
				);
		}
	}
	
	public static class PieChart extends GgplotWriter {

		public PieChart(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_bar(stat='identity', aes(x=1, y=Y, fill=factor(LABEL, levels=LABEL)))",
					"theme(axis.line.y=element_blank(),axis.text.x = element_blank(),axis.ticks=element_blank(),axis.title=element_blank(),legend.position='none',axis.line=element_blank())",
					"scale_fill_brewer(palette=schemeName, breaks=df$LABEL)",
					"scale_y_continuous(labels=rev(df$LABEL),breaks=head(((c(0,cumsum(rev(df$Y)))+c(cumsum(rev(df$Y)),0))/2),-1))"
					);
		}
		
	}
	
	public static class HeatMap extends GgplotWriter {

		public HeatMap(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			if (this.getChart().getConfig().scales.containsKey(Dimension.Z)) {
				this.getChart().getConfig().scales.put(Dimension.FILL, 
					this.getChart().getConfig().scales.get(Dimension.Z)
					);
			}
			this.getChart().getConfig().scales.remove(Dimension.Z);
			return Arrays.asList(
					"geom_tile(stat='identity', aes(x=X, y=Y, fill=Z))",
					"stat_contour(aes(x=X, y=Y, z=Z), colour='black')",
					//"scale_fill_distiller(name=\""+this.getChart().getConfig().getLabel(Dimension.Z)+"\",palette=schemeName)"
					this.getChart().getSeries().get(0).getScheme().getGGplotFillContinuous(
							this.getChart().getConfig().getLabel(Dimension.Z),
							this.getChart().getConfig().getMin(Dimension.FILL),
							this.getChart().getConfig().getMax(Dimension.FILL)
						)
					);
		}
		
	}
	
	public static class FacetedHeatMap extends HeatMap {

		public FacetedHeatMap(Chart chart) {
			super(chart);
		}
		
		public List<String> getPlots() {
			List<String> tmp = super.getPlots();
			tmp.add("facet_grid(factor(X_OUTER) ~ factor(Y_OUTER))");
			return tmp;
		}
	}
	
	public static class VectorFieldChart extends GgplotWriter {

		public VectorFieldChart(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_segment(aes(x=X, xend=X+DX, y=Y, yend=Y+DY),arrow = arrow(length = unit(0.1,\"cm\")))"//,
					//"stat_contour(aes(x=X, y=Y, z=Z))",
					//"scale_fill_distiller(palette=schemeName)"
					);
		}
		
	}

	
}
