package uk.co.terminological.simplechart;

import static uk.co.terminological.simplechart.Chart.Dimension.ID;
import static uk.co.terminological.simplechart.Chart.Dimension.STRENGTH;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Triple;

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
		
		Chart tmp = Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/chord"))
			.withNewChart("Hello", ChartType.CHORD)
			.withSeries(links)
				.bind(ID, t -> t.getFirst(), "source")
				.bind(STRENGTH, t -> t.getSecond())
				.bind(ID, t -> t.getThird(), "target")
				.withSorting(ID, (o1,o2) -> order.indexOf(o1)-(order.indexOf(o2)))
				.withColourScheme(ColourScheme.PuBuGn)
			.done();
			
		tmp.render();
		;
	}

}
