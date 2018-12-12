package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;


public class GnuplotWriter<X> extends Writer<X> {

	public static <X> void write(Chart<X> chart) throws IOException, TemplateException {
		GnuplotWriter<X> out = new GnuplotWriter<X>(chart);
		out.process();
	}
	
	public GnuplotWriter(Chart<X> chart) {
		
	}
		
	@Override
	protected String extractData() {
		List<String> out = new ArrayList<>();
		
		StringBuilder tmp = new StringBuilder();
		for (Triple<Chart.Dimension,Function<X,Object>,String> binding: chart.bindings) {
			tmp.append(binding.getFirst().toString()+
					binding.getThird() == null ? "" : " ("+binding.getThird()+")"
					+"\t");
		}
		out.add("# "+tmp.toString().trim());
		
		for (X item: chart.data) {
			tmp = new StringBuilder();
			for (Triple<Chart.Dimension,Function<X,Object>,String> binding: chart.bindings) {
				tmp.append(binding.getSecond().apply(item).toString()+"\t");
			}
			out.add(tmp.toString().trim());
		}
		
		return out.stream().collect(Collectors.joining("\n"));
	}

	@Override
	protected void process() throws IOException, TemplateException {
		File f = chart.getFile("gplot");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		template.process(root, out);
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
