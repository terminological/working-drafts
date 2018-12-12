package uk.co.terminological.simplechart;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.NotImplementedException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Common config parameters for multiple plots.
 * Also support for mulitplot one day.
 * @author terminological
 *
 */
public class Figure {

	public static class Parameter {

		public static List<Double> fromRange(double start, double end) {
			return fromRange(start,end,1000);
		}
		
		public static List<Double> fromRange(double start, double end, int samples) {
			List<Double> out = new ArrayList<>();
			double delta = (end-start)/samples;
			for (double s = start; s <= end; s+=delta) {
				out.add(s);
			}
			return out;
		}
	}

	List<Chart<?>> charts = new ArrayList<>();
	File workingDirectory;
	String title;
	String filename;
	Configuration cfg;

	public String getTitle() {return title;}
	public int getCharts() {return charts.size();}
	public boolean isMultiplot() {return getCharts() > 1;}
	
	private Figure(String title, File workingDirectory) {
		if (workingDirectory == null) {
			try { 
				this.workingDirectory = Files.createTempDirectory("gnuplotTmp").toFile();

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			this.workingDirectory = workingDirectory;
		}
		this.workingDirectory.mkdirs();
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
		cfg.setClassForTemplateLoading(Figure.class, "/gnuplot");
		this.title = title;
		filename = title.replaceAll("[^a-zA-Z0-9]+", "_");
	}

	public Figure withTemplateClass(Class<?> base) {
		cfg.setClassForTemplateLoading(base, "/");
		return this;
	}

	public static Figure outputTo(File directory) {
		return new Figure("No title",directory);
	}

	public <X> Data<X> withDefaultData(X data) {
		return withDefaultData(Collections.singletonList(data));
	}
	
	public <X> Data<X> withDefaultData(List<X> data) {
		Data<X> out = new Data<X>(title, workingDirectory);
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
		Chart<X> out = new Chart<X>(data,title, getTemplate(chartType), chartType.getWriter(),workingDirectory, this);
		this.charts.add(out);
		return out;
	}

	/*public <X> Chart<X> withNewChart(List<X> data, String title, String resourceName, ) {
		Chart<X> out = new Chart<X>(data,title, getTemplate(resourceName),workingDirectory, this);
		this.charts.add(out);
		return out;
	}

	public <X> Chart<X> withNewChart(List<X> data, String title, File templateFile) {
		Chart<X> out = new Chart<X>(data,title, getTemplate(templateFile),workingDirectory, this);
		this.charts.add(out);
		return out;
	}*/

	public void render() {
		throw new NotImplementedException("Will render the mulitplot");
	}
	
	public static class Data<X> extends Figure {

		private Data(String title, File directory) {
			super(title, directory);
		}

		List<X> defaultData;

		public Chart<X> withNewChart(String title, ChartType chartType) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(chartType),chartType.getWriter(),workingDirectory, this);
			this.charts.add(out);
			return out;
		}

		/*public Chart<X> withNewChart(String title, String resourceName) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(resourceName),workingDirectory, this);
			this.charts.add(out);
			return out;
		}

		public Chart<X> withNewChart(String title, File templateFile) {
			Chart<X> out = new Chart<X>(defaultData,title, getTemplate(templateFile),workingDirectory, this);
			this.charts.add(out);
			return out;
		}*/
	}

}
