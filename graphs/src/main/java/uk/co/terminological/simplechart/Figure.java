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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Common config parameters for multiple plots.
 * Also support for mulitplot one day.
 * @author terminological
 *
 */
public class Figure {

	List<Chart> charts = new ArrayList<>();
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
		this.withTitle(title);
		
	}

	public Figure withTemplateClass(Class<?> base) {
		cfg.setClassForTemplateLoading(base, "/");
		return this;
	}

	public static Figure outputTo(File directory) {
		return new Figure("No title",directory);
	}

	protected Optional<Template> getTemplate(ChartType chartType) {
		try {
			if (chartType.getTemplateFilename()==null) return Optional.empty();
			return Optional.of(cfg.getTemplate(chartType.getTemplateFilename()));
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

	public Chart withNewChart(String title, ChartType chartType) {
		Chart out = new Chart(title, getTemplate(chartType), chartType.getWriter(),workingDirectory, this);
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

	public void render(Integer cols, boolean includePlotTitles) {
		try {
		Template wrapper = this.getTemplate("ggplotMultiplot.ftl");
		List<String> tmp = new ArrayList<>();
		
		this.charts.forEach(c -> tmp.add(c.renderForMultiplot(includePlotTitles)));
		
		File outFile = getFile("png");
		PrintWriter pw = new PrintWriter(new FileWriter(getFile("R")));
		//getRoot()
		Map<String,Object> wrapperRoot = new HashMap<>();
		wrapperRoot.put("output", outFile.getAbsolutePath());
		wrapperRoot.put("plots", tmp);
		wrapperRoot.put("title", this.getTitle());
		wrapperRoot.put("cols", cols);
		wrapperRoot.put("includePlotTitles", includePlotTitles);
		wrapper.process(wrapperRoot, pw);
		pw.close();
		
		Chart.log.info("Starting R...");
		
		Process process2 = new ProcessBuilder("/usr/bin/R","-f",getFile("R").getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		IOUtils.copy(process2.getErrorStream(),System.out);
		
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
			Desktop.getDesktop().open(outFile);
		}
		
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}

	}
	
	
	public File getFile(String extension) {return new File(workingDirectory,filename+"."+extension);}
	
	public static class Data extends Figure {

		private Data(String title, File directory) {
			super(title, directory);
		}

		public Chart withNewChart(String title, ChartType chartType) {
			Chart out = new Chart(title, getTemplate(chartType),chartType.getWriter(),workingDirectory, this);
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

	public static Figure outputTo(Path path) {
		return outputTo(path.toFile());
	}
	public Figure withTitle(String string) {
		this.title = string;
		this.filename = title.replaceAll("[^a-zA-Z0-9]+", "_");
		return this;
	}

}
