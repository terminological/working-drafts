package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;


public class GnuplotWriter extends Writer {

	public static  void write(Chart chart) throws IOException, TemplateException {
		GnuplotWriter out = new GnuplotWriter(chart);
		out.process();
	}
	
	public GnuplotWriter(Chart chart) {
		super(chart);
	}
		
	@Override
	protected String extractData() {
		//TODO: Multiple series support
		// https://stackoverflow.com/questions/8534030/plotting-multiple-series-from-file-using-gnuplot
		// 2 blank lines - i.e. 3 newlines
		// referenced using index 0,1,2
		
		return getChart().getSeries().stream()
				.map(s -> extractData(s)).collect(Collectors.joining("\n\n\n"));
		
		
	}
	
	private <X> String extractData(Series<X> series) {
		
		StringBuilder tmp = new StringBuilder();
		tmp.append("# ");
		
		for (Triple<Chart.Dimension,Function<X,Object>,String> binding: series.getBindings()) {
			tmp.append(binding.getFirst().toString()+
					binding.getThird() == null ? "" : " ("+binding.getThird()+")"
					+"\t");
		}
		
		for (X item: series.getData()) {
			tmp.append("\n");
			for (Triple<Chart.Dimension,Function<X,Object>,String> binding: series.getBindings()) {
				tmp.append(binding.getSecond().apply(item).toString()+"\t");
			}
		}
		
		return tmp.toString();
	}

	@Override
	protected void process() throws IOException, TemplateException {
		File f = getChart().getFile("gplot");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().process(getRoot(), out);
		out.close();
		Chart.log.info("Starting GNUPlot...");
		
		Process process2 = new ProcessBuilder("/usr/bin/gnuplot","-c",f.getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending GNUPlot...");
	}
	
	
	
}
