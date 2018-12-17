package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.Triple;

public class GgplotWriter extends Writer {

	public static  void write(Chart chart) throws IOException, TemplateException {
		GnuplotWriter out = new GnuplotWriter(chart);
		out.process();
	}
	
	public GgplotWriter(Chart chart) {
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
		
		StringBuilder dfConstruct = new StringBuilder("df <- data.frame(");
		StringBuilder vector = new StringBuilder();
		
		for (Triple<Chart.Dimension,Function<X,Object>,String> binding: series.getBindings()) {
			String varName = binding.getFirst().toString()+
					binding.getThird() == null ? "" : "_"+binding.getThird();
			
			vector.append(varName+" <- c[");
			
			vector.append(
					series.getData().stream().map(binding.getSecond())
						.map(GgplotWriter::format)
						.collect(Collectors.joining(", "))
						);
			
			
			vector.append("];\n");
			
			if (dfConstruct.length()>0) dfConstruct.append(",");
			dfConstruct.append(varName);
			
		}
		
		dfConstruct.append(");\n");
		
		return vector.toString()+"\n"+dfConstruct.toString();
	}

	//TODO: localDate support?
	private static String format(Object o) {
		if (o instanceof Number) return o.toString();
		return "'"+o.toString()+"'";
	}
	
	@Override
	protected void process() throws IOException, TemplateException {
		File f = getChart().getFile("R");
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().get().process(getRoot(), out);
		out.close();
		Chart.log.info("Starting R...");
		
		Process process2 = new ProcessBuilder("/usr/bin/R","-c",f.getAbsolutePath())
		.redirectOutput(Redirect.INHERIT)
		.start();
		
		try {
			System.out.println(process2.waitFor());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Chart.log.info("Ending R...");
	}
	
	
	
}