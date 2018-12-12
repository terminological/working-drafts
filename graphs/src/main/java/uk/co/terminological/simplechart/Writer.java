package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class Writer<X> {

	protected Chart<X> chart;
	protected Template template;
	protected Map<String,Object> root = new HashMap<String,Object>();

	public Writer(Chart<X> chart) {
		this.chart = chart;
		this.template = chart.template;
		root.put("data", extractData());
		root.put("config", chart.config());
		for (Entry<String,String> custom: this.chart.customField.entrySet()) {
			root.put(custom.getKey(), custom.getValue());
		}
	}

	protected abstract void process() throws IOException, TemplateException;

	protected abstract String extractData();

	public Writer() {
		super();
	}

}