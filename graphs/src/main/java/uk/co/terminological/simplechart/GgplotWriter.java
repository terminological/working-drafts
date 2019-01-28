package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;

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
		
		StringBuilder dfConstruct = new StringBuilder();
		StringBuilder vector = new StringBuilder();
		
		for (Triple<Chart.Dimension,Function<X,Object>,String> binding: series.getBindings()) {
			String varName = binding.getFirst().name()+
					(binding.getThird() == "" ? "" : "_"+binding.getThird());
			
			vector.append(varName+" <- c(");
			
			vector.append(
					series.getData().stream().map(binding.getSecond())
						.map(GgplotWriter::format)
						.collect(Collectors.joining(", "))
						);
			
			
			vector.append(");\n");
			
			if (dfConstruct.length()>0) 
				dfConstruct.append(",");
			else 
				dfConstruct.append("df <- data.frame(");
			dfConstruct.append(varName);
			
		}
		
		dfConstruct.append(");\n");
		
		return vector.toString()+"\n"+dfConstruct.toString();
	}

	//TODO: localDate support?
	private static String format(Object o) {
		if (o == null) return "NA";
		if (o instanceof Number) return o.toString();
		return "'"+o.toString().replace("'", "\\'")+"'";
	}
	
	@Override
	protected void process() throws IOException, TemplateException {
		
		getRoot().put("plots", getPlots());
		//TODO: how to control this?
		//TODO: colour schemes?
		getRoot().put("schemeName", getChart().getSeries().stream().map(s -> s.getScheme().getName()).findFirst().orElse("Set1"));
		
		
		getRoot().put("output", getChart().getFile("png").getAbsolutePath());
		
		File f = getChart().getFile("R");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().get().process(getRoot(), out);
		out.close();
		Chart.log.info("Starting R...");
		
		Process process2 = new ProcessBuilder("/usr/bin/R","-f",f.getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending R...");
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
					"geom_smooth(aes(x=X, y=Y))"
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
					"geom_bar(stat='identity', aes(x=1, y=Y, fill=LABEL))",
					"theme(axis.line.y=element_blank(),axis.text.y=element_blank(),axis.ticks.y=element_blank(),axis.title.x=element_blank())",
					"scale_fill_brewer(palette=schemeName)",
					"coord_flip()"
					);
		}
		
	}
	
}
