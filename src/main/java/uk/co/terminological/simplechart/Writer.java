package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Tuple;


public class Writer {

	private Chart<?> chart;
	private Template template;
	private Map<String,Object> root = new HashMap<String,Object>();
	
	public static <X> void write(Chart<X> chart) throws IOException, TemplateException {
		Writer out = new Writer(chart);
		out.template = chart.template;
		out.process();
	}
	
	public Writer(Chart<?> chart) {
		this.chart = chart;
		root.put("data", extractData(chart));
		root.put("config", chart.config());
		for (Entry<String,String> custom: this.chart.customField.entrySet()) {
			root.put(custom.getKey(), custom.getValue());
		}
	}
		
	private static <X> List<String> extractData(Chart<X> chart) {
		List<String> out = new ArrayList<>();
		
		StringBuilder tmp = new StringBuilder();
		for (Tuple<Chart.Dimension,Function<X,Object>> binding: chart.bindings) {
			tmp.append(binding.getFirst().toString()+"\t");
		}
		out.add("# "+tmp.toString().trim());
		
		for (X item: chart.data) {
			tmp = new StringBuilder();
			for (Tuple<Chart.Dimension,Function<X,Object>> binding: chart.bindings) {
				tmp.append(binding.getSecond().apply(item).toString()+"\t");
			}
			out.add(tmp.toString().trim());
		}
		
		return out;
	}

	private void process() throws IOException, TemplateException {
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
