package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import freemarker.template.TemplateException;

public abstract class D3JSWriter extends Writer {

	
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
	public class Matrix extends D3JSWriter {

		@Override
		// expecting some form of labelled triple plus some form of ordering for
		// each individual x and y label 
		protected String extractData() {
			
			//TODO find out x labels and x label order.
			//find out y labels and y label order.
			//find out value for x and y and put them in an ordered grid. 
			//EavMap class....
		}

	}

	/**
	 * Processes an data input that can be bound to a weighted edge. 
	 * @author rc538
	 *
	 * @param <Y>
	 */
	public class Nodes extends D3JSWriter {

		@Override
		protected String extractData() {
			
			//TODO more robust method for determining nodes and edges than order
			Series<?> nodes = this.getChart().getSeries().get(0);
			Series<?> edges = this.getChart().getSeries().get(1);
			
			
			StringBuilder builder = new StringBuilder();
			
			labelGenerator = nodes
			
		}

	}
	
}
