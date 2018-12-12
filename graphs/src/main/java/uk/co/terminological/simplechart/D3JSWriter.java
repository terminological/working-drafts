package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import freemarker.template.TemplateException;

public abstract class D3JSWriter<X> extends Writer<X> {

	
	@Override
	protected void process() throws IOException, TemplateException {
		File f = getChart().getFile("html");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().process(getRoot(), out);
		out.close();
		Chart.log.info("Writing html to: "+f.getAbsolutePath());
		
		//TODO: Kick of a headless chrome instance and simulate click on download SVG item
		// or just get user to open file in chrome
	}

	/**
	 * Processes an data input that can be bound to a weighted edge. 
	 * @author rc538
	 *
	 * @param <Y>
	 */
	public class Matrix<Y> extends D3JSWriter<Y> {

		@Override
		protected String extractData() {
			getChart().
		}

	}

}
