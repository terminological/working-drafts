package uk.co.terminological.simplechart;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class Figure {

	List<Chart<?>> charts = new ArrayList<>();
	File workingDirectory;
	Configuration cfg;
	
	private Figure() {
		try { 
			workingDirectory = Files.createTempDirectory("gnuplotTmp").toFile();
			workingDirectory.mkdirs();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
		cfg.setClassForTemplateLoading(Figure.class, "/gnuplot");
	}
	
	public Figure withTemplateClass(Class<?> base) {
		cfg.setClassForTemplateLoading(base, "/");
		return this;
	}
	
	public Figure withOutputDirectory(File directory) {
		this.workingDirectory = directory;
		return this;
	}
	
	public <X> Data<X> withDefaultData(List<X> data) {
		Data<X> out = new Data<X>();
		out.cfg = this.cfg;
		out.charts = this.charts;
		out.defaultData = data;
		out.workingDirectory = this.workingDirectory;
		return out;
	}
	
	protected Template getTemplate(ChartType chartType) {
		try {
			return cfg.getTemplate(chartType.getTemplateFilename());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Template getTemplate(String resourceName) {
		try {
			return cfg.getTemplate(resourceName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Template getTemplate(File templateFile) {
		try {
			return cfg.getTemplate(templateFile.getAbsolutePath());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public <X> Chart<X> withNewChart(List<X> data, String title, ChartType chartType) {
		Chart<X> out = new Chart<X>(data,title, getTemplate(chartType),workingDirectory);
		this.charts.add(out);
		return out;
	}
	
	public <X> Chart<X> withNewChart(List<X> data, String title, String resourceName) {
		Chart<X> out = new Chart<X>(data,title, getTemplate(resourceName),workingDirectory);
		this.charts.add(out);
		return out;
	}
	
	public <X> Chart<X> withNewChart(List<X> data, String title, File templateFile) {
		Chart<X> out = new Chart<X>(data,title, getTemplate(templateFile),workingDirectory);
		this.charts.add(out);
		return out;
	}
	
	public static class Data<X> extends Figure {
		
		List<X> defaultData;
		
		public Chart<X> withNewChart(String title, ChartType chartType) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(chartType),workingDirectory);
			this.charts.add(out);
			return out;
		}
		
		public Chart<X> withNewChart(String title, String resourceName) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(resourceName),workingDirectory);
			this.charts.add(out);
			return out;
		}
		
		public Chart<X> withNewChart(String title, File templateFile) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(templateFile),workingDirectory);
			this.charts.add(out);
			return out;
		}
	}
	
}
