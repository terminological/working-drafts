package uk.co.terminological.simplechart;

import java.io.IOException;
import java.util.List;

import freemarker.template.TemplateException;

import static uk.co.terminological.simplechart.Chart.Dimension.*;
import static uk.co.terminological.simplechart.OutputTarget.*;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Tuple;

public class TestSimpleChart {

	public static void main(String[] args) throws IOException, TemplateException {
		
		List<Tuple<Double,Double>> example = FluentList.create(
				Tuple.create(1D, 3D),
				Tuple.create(2D, 4D),
				Tuple.create(3D, 3D),
				Tuple.create(4D, 2D),
				Tuple.create(5D, 3D)
				);
		
		Chart.create(example)
			.bind(X, t -> t.getFirst())
			.bind(Y, t -> t.getSecond())
			.config()
				.withOutputTarget(SCREEN)
				.withTitle("Hello")
				.withXLabel("x-axis")
				.withYLabel("y-axis")
			.done().render(ChartType.XY_LINE);;
			
		;
	}

}
