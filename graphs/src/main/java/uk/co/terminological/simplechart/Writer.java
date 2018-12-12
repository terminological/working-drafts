package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class Writer<X> {

	protected Chart<X> chart;

	protected abstract void process() throws IOException, TemplateException;

	protected abstract String extractData();

	protected Template template;
	protected Map<String,Object> root = new HashMap<String,Object>();

	public Writer() {
		super();
	}

}