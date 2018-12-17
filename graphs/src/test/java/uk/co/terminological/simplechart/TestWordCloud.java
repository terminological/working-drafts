package uk.co.terminological.simplechart;

import static uk.co.terminological.simplechart.Chart.Dimension.ID;
import static uk.co.terminological.simplechart.Chart.Dimension.LABEL;
import static uk.co.terminological.simplechart.Chart.Dimension.STRENGTH;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import freemarker.template.TemplateException;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.datatypes.Tuple;

public class TestWordCloud {

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
				Triple.create(2, 0.5, 3),
				Triple.create(3, 0.1, 4),
				Triple.create(4, 0.5, 5),
				Triple.create(5, 1.0, 1)
				);
		
		Chart tmp = Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/network"))
			.withNewChart("Hello", ChartType.NETWORK)
			.withSeries(nodes)
				.bind(ID, t -> t.getFirst())
				.bind(LABEL, t -> t.getSecond())
			.done()
			.withSeries(links)
				.bind(ID, t -> t.getFirst(), "source")
				.bind(STRENGTH, t -> t.getSecond())
				.bind(ID, t -> t.getThird(), "target")
			.done()
			.config()
				.withXLabel("x-axis")
				.withYLabel("y-axis")
			.done();
		tmp.render();
		;
	}

}
