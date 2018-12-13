package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
		
		List<Triple<String,Double,String>> links = FluentList.create(
				Triple.create("one", 1.0, "two"),
				Triple.create("two", 0.5, "one"),
				Triple.create("two", 2.0, "three"),
				Triple.create("three", 1.0, "two"),
				Triple.create("three", 1.0, "four"),
				Triple.create("four", 0.5, "three"),
				Triple.create("four", 2.0, "five"),
				Triple.create("five", 1.0, "four"),
				Triple.create("five", 1.0, "one"),
				Triple.create("one", 0.5, "five")
				);
		
		List<String> order = Arrays.asList("one","two","three","four","five");
		
		Chart tmp = Figure.outputTo(new File("/home/terminological/tmp/chord"))
			.withNewChart("Hello", ChartType.CHORD)
			.withSeries(links)
				.bind(SOURCE, t -> t.getFirst())
				.bind(WEIGHT, t -> t.getSecond())
				.bind(TARGET, t -> t.getThird())
				.sorting(SOURCE, (o1,o2) -> order.indexOf(o1)-(order.indexOf(o2)))
			.done();
			
		tmp.render();
		;
	}

}
