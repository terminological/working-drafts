package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.simplechart.Chart.Dimension;

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
			
			return extractData(this.getChart().getSeries().get(0));
			
			//TODO find out x labels and x label order.
			//find out y labels and y label order.
			//find out value for x and y and put them in an ordered grid. 
			//EavMap class....
			
		}
		
		protected <X,Y> String extractData(Series<Y> edges) {
			Function<Y, Object> xGenerator = edges.functionFor(Dimension.SOURCE_ID);
			Function<Y, Object> yGenerator = edges.functionFor(Dimension.TARGET_ID);
			Function<Y, Object> valueGenerator = edges.functionFor(Dimension.WEIGHT);
			
			EavMap<Object,Object,Object> tmp = new EavMap<>();
			edges.data.forEach(y -> {
				tmp.add(xGenerator.apply(y), 
						yGenerator.apply(y),
						valueGenerator.apply(y));
						
			});
			
			Set<Object> xLabels = tmp.getEntitySet();
			Set<Object> yLabels = tmp.getAttributeSet();
			//TODO: A sorting function - should be part of the value
			
			xLabels.forEach(x -> {
				yLabels.forEach(y -> {
					Object value = tmp.get(x,y);
					//TODO - write this with missing values...
				});
			});
			
			throw new RuntimeException("not yet implemented");
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
			
			return extractData(
					this.getChart().getSeries().get(0),
					this.getChart().getSeries().get(1));
			
		}

		protected <X,Y> String extractData(Series<X> nodes, Series<Y> edges) {
			StringBuilder builder = new StringBuilder();
			
			Function<X, Object> labelGenerator = nodes.functionFor(Dimension.LABEL);
			Function<X, Object> idGenerator = nodes.functionFor(Dimension.ID);
			
			Function<Y, Object> sourceIdGenerator = edges.functionFor(Dimension.SOURCE_ID);
			Function<Y, Object> targetIdGenerator = edges.functionFor(Dimension.TARGET_ID);
			Function<Y, Object> weightGenerator = edges.functionFor(Dimension.WEIGHT);
			
			//TODO: Could probably have an optional<function<x,y>> accessor here... would it be useful though
			//for elements such as size, or fill or other node or relationship properties
			
			builder.append("const graph = { 'nodes': [");
			
			String tmp = nodes.data.stream().map(x -> {
				String label = labelGenerator.apply(x).toString();
				String id = idGenerator.apply(x).toString();
				return "{'id':'"+id+"','label':'"+label+"'}";
			}).collect(Collectors.joining(","));
			
			builder.append(tmp);
			builder.append("],'links': [");
			
			String tmp2 = edges.data.stream().map(y -> {
				String sourceId = sourceIdGenerator.apply(y).toString();
				String targetId = targetIdGenerator.apply(y).toString();
				String weight = weightGenerator.apply(y).toString();
				return "{'source':'"+sourceId+"','target':'"+targetId+"','weight':'"+weight+"'}";
			}).collect(Collectors.joining(","));
			
			builder.append(tmp2);
			builder.append("]};");
			return builder.toString();
		}
	}
	
	
	/*
	 * const graph = {
  "nodes": [
    {"id": "1", "group": 1},
    {"id": "2", "group": 2},
    {"id": "4", "group": 3},
    {"id": "8", "group": 4},
    {"id": "16", "group": 5},
    {"id": "11", "group": 1},
    {"id": "12", "group": 2},
    {"id": "14", "group": 3},
    {"id": "18", "group": 4},
    {"id": "116", "group": 5}
  ],
  "links": [
    {"source": "1", "target": "2", "value": 1},
    {"source": "2", "target": "4", "value": 1},
    {"source": "4", "target": "8", "value": 1},
    {"source": "4", "target": "8", "value": 1},
    {"source": "8", "target": "16", "value": 1},
    {"source": "16", "target": "1", "value": 1}
  ]
}
	 */
	
}
