package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class Writer {

	private Chart chart;
	private Optional<Template> template;
	private Map<String,Object> root = new HashMap<String,Object>();

	public Writer(Chart chart) {
		this.chart = chart;
		this.template = chart.getTemplate();
		root.put("data", extractData());
		root.put("config", chart.config());
		for (Entry<String,String> custom: this.chart.customField.entrySet()) {
			root.put(custom.getKey(), custom.getValue());
		}
	}

	public Chart getChart() {
		return chart;
	}

	public Template getTemplate() {
		return template;
	}

	public Map<String, Object> getRoot() {
		return root;
	}
	
	public String getData() {
		return root.get("data").toString();
	}
	
	protected abstract void process() throws IOException, TemplateException;

	protected abstract String extractData();

}