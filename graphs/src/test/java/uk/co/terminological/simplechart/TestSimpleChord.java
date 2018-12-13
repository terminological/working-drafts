package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import freemarker.template.TemplateException;

import static uk.co.terminological.simplechart.Chart.Dimension.*;
import static uk.co.terminological.simplechart.OutputTarget.*;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.datatypes.Tuple;

public class TestSimpleChord {

	public static void main(String[] args) throws IOException, TemplateException {
		
		BasicConfigurator.configure();
		
		List<Tuple<Integer,String>> nodes = FluentList.create(
				Tuple.create(1, "one"),
				Tuple.create(2, "two"),
				Tuple.create(3, "three"),
				Tuple.create(4, "four"),
				Tuple.create(5, "five")
				);
		
		List<Triple<Integer,Double,Integer>> links = FluentList.create(
				Triple.create(1, 1.0, 2),
				Triple.create(2, 2.0, 3),
				Triple.create(3, 1.0, 4),
				Triple.create(4, 2.0, 5),
				Triple.create(5, 1.0, 1)
				);
		
		Chart tmp = Figure.outputTo(new File("/home/terminological/tmp/gnuplot"))
			.withNewChart("Hello", ChartType.NETWORK)
			.withSeries(nodes)
				.bind(ID, t -> t.getFirst())
				.bind(LABEL, t -> t.getSecond())
			.done()
			.withSeries(links)
				.bind(SOURCE, t -> t.getFirst())
				.bind(WEIGHT, t -> t.getSecond())
				.bind(SOURCE, t -> t.getThird())
			.done()
			.config()
				.withXLabel("x-axis")
				.withYLabel("y-axis")
			.done();
		tmp.render();
		;
	}

}
