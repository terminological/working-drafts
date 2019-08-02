package uk.co.terminological.simplechart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang.NotImplementedException;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;

public abstract class GgplotWriter2 extends Writer {

	public abstract List<String> getPlots();
	
	public GgplotWriter2(Chart chart) {
		super(chart);
	}
		
	@Override
	protected String extractData() {
		
		// Multiple series support
		// https://stackoverflow.com/questions/8534030/plotting-multiple-series-from-file-using-gnuplot
		// 2 blank lines - i.e. 3 newlines
		// referenced using index 0,1,2
		return getChart().getSeries().stream()
			.map(s -> extractData(s)).collect(Collectors.joining("\n\n\n"));
		
	}
	
	private <X> String extractData(Series<X> series) {
		
		Set<String> names = 
				series.getBindings().stream()
				.filter(t -> !t.value().equals(getChart().config.getLabel(t.entity())))
				.map(t -> t.value())
				.collect(Collectors.toSet());
		
		int index = this.getChart().getSeries().indexOf(series);
		Path tsv =  this.getChart().getFile(""+index+".tsv").toPath();
		
		try {
			BufferedWriter w = Files.newBufferedWriter(tsv);
			// write headers
			w.append(series.getBindings().stream().map(t -> t.entity().name()).collect(Collectors.joining("\t"))+"\tNAME\n");
			series.getData().forEach(x -> {
				
				names.stream().sorted().forEach(name -> {
					
					String line = series.getBindings().stream()
						.filter(t -> t.value() == getChart().config.getLabel(dim) || t.value().equals(name))
						.map(t -> t.attribute().apply(x))
						.map(GgplotWriter2::format)
						.collect(Collectors.joining("\t"));
					
					line = line+"\t"+name;
				
				try {
					w.append(line+"\n");
				} catch (IOException e) {
					new RuntimeException(e);
				}
			});
			});
			w.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		/*StringBuilder dfConstruct = new StringBuilder();
		StringBuilder vectors = new StringBuilder();
		StringBuilder nameVec = new StringBuilder();
		
		
		int size = series.getData().size() * (names.size()==0?1:names.size());
		
		dfConstruct.append("order <- seq(0,"+(size-1)+");\n");
		dfConstruct.append("df <- data.frame(order");
			
		dimensions.stream().forEach(dim -> {
			String varName = dim.name();
			StringBuilder vector = new StringBuilder();
			names.stream().sorted().forEach(name -> {
			
				series.getBindings().stream().filter(t -> t.entity().equals(dim))
					.filter(t -> t.value() == getChart().config.getLabel(dim) || t.value().equals(name))
					.forEach(binding -> {
						if (vector.length() > 0) {
							vector.append(", ");
						}
						vector.append(
								series.getData().stream().map(binding.getSecond())
									.map(GgplotWriter2::format)
									.collect(Collectors.joining(", "))
									);
					
				});
			});
			vector.insert(0,"tmp_"+varName+" <- c(");
			vector.append(");");
			vectors.append(vector+"\n");
			dfConstruct.append(",");
			dfConstruct.append(varName+"=tmp_"+varName);

		});

		names.stream().sorted().forEach(name -> {
			series.getData().stream().forEach(d -> {
				nameVec.append((nameVec.length() == 0 ? "":",")+format(name));
			});
		});
		nameVec.insert(0, "name <- c(");
		nameVec.append(");");
		
		dfConstruct.append(",NAME=name,stringsAsFactors = FALSE);\n");
		
		return vectors.toString()+"\n"+nameVec.toString()+"\n"+dfConstruct.toString();*/
		return "df <- read_tsv(\""+tsv.toAbsolutePath()+"\")";
	}

	//TODO: localDate support?
	private static String format(Object o) {
		if (o == null) return "NA";
		if (o instanceof Double) {
			if (Double.isNaN((Double) o)) return "NaN";
			if (o.equals(Double.NEGATIVE_INFINITY)) return "-Inf";
			if (o.equals(Double.POSITIVE_INFINITY)) return "Inf";
		}
		if (o instanceof Number) return o.toString();
		return "'"+o.toString().replace("'", "\\'")+"'";
	}
	
	@Override
	protected Path process() throws IOException, TemplateException {
		
		getRoot().put("plots", getPlots());
		//TODO: how to control this?
		//TODO: colour schemes?
		getRoot().put("schemeName", getChart().getSeries().stream().map(s -> s.getScheme().getName()).findFirst().orElse("Set1"));
		getRoot().put("includePlotTitles", true);
		
		File outFile = getChart().getFile("png");
		//getRoot()
		Map<String,Object> wrapperRoot = new HashMap<>();
		wrapperRoot.put("output", outFile.getAbsolutePath());
		
		Template wrapper = this.getChart().getFigure().getTemplate("ggplotSingleplot.ftl");
		
		File f = getChart().getFile("R");
		PrintWriter fileOut = new PrintWriter(new FileWriter(f));
		
		StringBuilderWriter out = new StringBuilderWriter();
		getTemplate().get().process(getRoot(), out);
		out.close();
		wrapperRoot.put("plot", out.toString());
		wrapperRoot.put("title", getChart().getConfig().getTitle());
		wrapper.process(wrapperRoot, fileOut);
		fileOut.close();
		
		Chart.log.info("Starting R...");
		
		Process process2 = new ProcessBuilder("/usr/bin/R","-f",f.getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		IOUtils.copy(process2.getErrorStream(),System.out);
		
		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending R...");
		return outFile.toPath();
	}
	
	@Override
	protected String processForMultiplot(boolean includePlotTitles) throws IOException, TemplateException {
		
		getRoot().put("plots", getPlots());
		getRoot().put("includePlotTitles", includePlotTitles);
		getRoot().put("schemeName", getChart().getSeries().stream().map(s -> s.getScheme().getName()).findFirst().orElse("Set1"));
		
		StringBuilderWriter out = new StringBuilderWriter();
		getTemplate().get().process(getRoot(), out);
		out.close();
		return out.toString();
	}
	
	public static class MultiBarChart extends GgplotWriter2 {

		public MultiBarChart(Chart chart) {
			super(chart);
		}

		@Override
		public List<String> getPlots() {
			return Arrays.asList(
					"geom_bar(stat='identity', aes(x=X, y=Y, colour=factor(NAME)))",
					"scale_colour_brewer(palette=schemeName, name=NULL)"
					);
		}

		
		
	}
	
	public static class MultiScatterLine extends GgplotWriter2 {

		public MultiScatterLine(Chart chart) {
			super(chart);
			
		}

		@Override
		public List<String> getPlots() {
			boolean hasLines = this.getChart().getSeries().stream()
					.anyMatch(s -> s.getBindings().stream()
							.anyMatch(t -> t.getFirst().equals(Dimension.Y_LINE)));
			boolean hasPoints = this.getChart().getSeries().stream()
					.anyMatch(s -> s.getBindings().stream()
							.anyMatch(t -> t.getFirst().equals(Dimension.Y)));
			List<String> out = new ArrayList<>();
			if (hasPoints) out.add("geom_point(stat='identity', aes(x=X, y=Y, colour=factor(NAME)))");
			if (hasLines) out.add("geom_line(stat='identity', aes(x=X, y=Y_LINE, colour=factor(NAME)))");
			if (hasPoints && !hasLines) out.add("geom_smooth(aes(x=X, y=Y, colour=factor(NAME)), method = 'glm')");
			out.add("scale_colour_brewer(palette=schemeName, name=NULL)");
			return out;
		}
	}
	

	
}
