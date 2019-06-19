package uk.co.terminological.costbenefit;

import static org.junit.Assert.*;
import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;
import static uk.co.terminological.simplechart.Chart.Dimension.Y_FIT;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.SeriesBuilder;

public class ClassifierSimulation {

	
	Figure figures;
	
	@Before
	public void setUp() throws Exception {
		figures = Figure.outputTo(Files.createTempDirectory("diag"));
	}

	@Test
	public final void test() throws IOException, TemplateException {
		figures.withNewChart("hx", ChartType.XY_LINE)
		.config().withXScale(0F, 1F)
		.withXLabel("x")
		.withYLabel("gauss")
		.withYScale(1F, 1F)
		.done()
		.withSeries(SeriesBuilder.range(-1D, 1D, 1000))
		.bind(X, t -> t)
		.bind(Y, t -> GaussianCDF.value(0, 1))
		.done()
		.render();
	}

}
