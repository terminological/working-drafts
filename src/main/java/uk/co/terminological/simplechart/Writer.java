package uk.co.terminological.simplechart;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateExceptionHandler;


public class Writer {

	private Configuration cfg;
	
	public static <X> void write(Chart<X> chart) {
		
		
		
	}

	public Writer() {
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
		cfg.setClassForTemplateLoading(Writer.class, "/freemarker");
	}
	
}
