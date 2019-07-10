package uk.co.terminological.simplechart;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;


public class GnuplotWriter extends Writer {

	
	public GnuplotWriter(Chart chart) {
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
		
		StringBuilder tmp = new StringBuilder();
		tmp.append("# ");
		
		for (Triple<Chart.Dimension,Function<X,? extends Object>,String> binding: series.getBindings()) {
			tmp.append(binding.getFirst().toString()+
					binding.getThird() == null ? "" : " ("+binding.getThird()+")"
					+"\t");
		}
		
		for (X item: series.getData()) {
			tmp.append("\n");
			for (Triple<Chart.Dimension,Function<X,? extends Object>,String> binding: series.getBindings()) {
				tmp.append(toGnuPlotString(binding.getSecond().apply(item))+"\t");
			}
		}
		
		return tmp.toString();
	}

	@Override
	protected Path process() throws IOException, TemplateException {
		
		File f = getChart().getFile("gplot");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().get().process(getRoot(), out);
		out.close();
		Chart.log.info("Starting GNUPlot...");
		
		try {
			Process process2 = new ProcessBuilder("/usr/bin/gnuplot","-c",f.getAbsolutePath())
			.redirectOutput(Redirect.INHERIT)
			.start();
			
			System.out.println("Gnuplot status: "+process2.waitFor());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending GNUPlot...");
		return getChart().getFile("png").toPath();
		
		
	}
	
	@Override
	protected String processForMultiplot(boolean includePlotTitles) throws IOException, TemplateException {
		throw new NotImplementedException();
	}
	
	private static String toGnuPlotString(Object o) {
		DecimalFormat df = new DecimalFormat("0.000000"); 
		if (o instanceof Double) {
			if (((Double) o).isNaN() || ((Double) o).isInfinite()) return "";
			return df.format((Double) o);
		}
		return o.toString();
	}
	
}
