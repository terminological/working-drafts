package uk.co.terminological.simplechart;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Template;
import freemarker.template.TemplateException;

//TODO: integrate with https://github.com/jtablesaw/tablesaw
public class Chart {

	List<Series<?>> series = new ArrayList<>();
	Optional<Template> template;
	Figure figure;
	Config config;
	Map<String,String> customField = new HashMap<>();
	String filename;
	Class<? extends Writer> writerCls;
	
	public static Logger log = LoggerFactory.getLogger(Chart.class);
	
	protected Chart(String title, Optional<Template> template, Class<? extends Writer> class1, File workingDirectory, Figure figure) {
		this.template = template;
		this.figure = figure;
		this.filename = title.replaceAll("[^a-zA-Z0-9]+", "_");
		this.writerCls = class1;
		this.config = Config.create(this, title);
		log.info("Chart at: directory="+workingDirectory+"; file="+filename);
	}
	
	
	
	public Chart withConfig(Config config) {
		this.config = config;
		this.config.chart = this;
		return this;
	}
	
	public Chart withAxes(String x,String y) {
		this.config().withXLabel(x);
		this.config().withYLabel(y);
		return this;
	}
	
	public Chart withCustomField(String key, Object value) {
		if (Arrays.asList("data","config").contains(key)) throw new RuntimeException("Cannot use the custom key: "+key);
		this.customField.put(key, value.toString());
		return this;
	}
	
	public Config config() {return config;}
	
	public void render() {
		Writer writer;
		try {
			writer = writerCls.getDeclaredConstructor(Chart.class).newInstance(this);
			Path out = writer.process();
			
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Path h = this.getFile("html").toPath();
				if (!h.equals(out)) {
					BufferedWriter w = Files.newBufferedWriter(h);
					w.write("<html><head></head><body><img src='"+h.getParent().relativize(out).toString()+"'></body></html>");
					w.close();
				}
			    Desktop.getDesktop().browse(h.toUri());
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException |IOException | TemplateException e) {
			throw new RuntimeException(e);
		} 
	}
	
	protected String renderForMultiplot(boolean includePlotTitles) {
		Writer writer;
		try {
			writer = writerCls.getDeclaredConstructor(Chart.class).newInstance(this);
			String out = writer.processForMultiplot(includePlotTitles);
			return out;
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException |IOException | TemplateException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL,Y_LOW,Y_HIGH,Y_FIT,FILL,
		ID,STRENGTH,TEXT,DX,DY,DZ
	}
	
	public File getWorkingDirectory() {
		return figure.workingDirectory;
	}
	
	public File getFile(String extension) {return new File(getWorkingDirectory(),filename+"."+extension);}


	public <X> Series<X> withSeries(Stream<X> nodes) {
		return withSeries(nodes.collect(Collectors.toList()));
	}

	public <X> Series<X> withSeries(List<X> nodes) {
		Series<X> tmp = new Series<X>(nodes, this);
		this.series.add(tmp);
		return tmp;
	}

	//================ Getters generated

	protected List<Series<?>> getSeries() {
		return series;
	}



	protected Optional<Template> getTemplate() {
		return template;
	}



	protected Figure getFigure() {
		return figure;
	}



	protected Config getConfig() {
		return config;
	}



	protected Map<String, String> getCustomField() {
		return customField;
	}



	protected String getFilename() {
		return filename;
	}



	protected Class<? extends Writer> getWriterCls() {
		return writerCls;
	}



	protected static Logger getLog() {
		return log;
	}
}
