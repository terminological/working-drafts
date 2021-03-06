package uk.co.terminological.simplechart;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import freemarker.template.TemplateException;

import static uk.co.terminological.simplechart.Chart.Dimension.*;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Tuple;

public class TestSimpleChart {

	public static void main(String[] args) throws IOException, TemplateException {
		
		BasicConfigurator.configure();
		
		List<Tuple<Double,Double>> example = FluentList.create(
				Tuple.create(1D, 3D),
				Tuple.create(2D, 4D),
				Tuple.create(3D, 3D),
				Tuple.create(4D, 2D),
				Tuple.create(5D, 3D)
				);
		
		Chart tmp = Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/gnuplot"))
			
			.withNewChart("Hello", ChartType.XY_LINE)
			.withSeries(example)
				.bind(X, t -> t.getFirst())
				.bind(Y, t -> t.getSecond()+Math.random()-0.5D)
				.bind(Y_FIT, t -> t.getSecond())
			.done()
			.config()
				.withXLabel("x-axis")
				.withYLabel("y-axis")
			.done();
		tmp.render();
		;
	}

}
