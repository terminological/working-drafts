package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import uk.co.terminological.datatypes.Tuple;


public class Writer {

	private Configuration cfg;
	private Chart<?> chart;
	private Template template;
	
	public static <X> void write(Chart<X> chart, ChartType type) throws IOException {
		Writer out = new Writer(chart);
		out.template = out.cfg.getTemplate(type.getTemplateFilename());
		
	}
	
	public static <X> void write(Chart<X> chart, File template) throws IOException {
		Writer out = new Writer(chart);
		out.template = out.cfg.getTemplate(template.getAbsolutePath());
		
	}
	
	public static <X> void write(Chart<X> chart, Class<?> callingClass, String resource) throws IOException {
		Writer out = new Writer(chart);
		out.cfg.setClassForTemplateLoading(callingClass, "/");
		out.template = out.cfg.getTemplate(resource);
	}
	

	public Writer(Chart<?> chart) {
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
		cfg.setClassForTemplateLoading(Writer.class, "/gnuplot");
		Map<String,Object> root = new HashMap<String,Object>();
		root.put("data", extractData(chart));
		root.put("config", chart.config());
		
	}
		
	private <X> List<String> extractData(Chart<X> chart) {
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

	
	
	
	
}
