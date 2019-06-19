package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.SeriesBuilder;

public class ClassifierSimulation {

	
	Figure figures;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception {
		
		figures = Figure.outputTo(Files.createTempDirectory("diag"));
	}

	@Test
	public void test123() throws IOException, TemplateException {
		figures.withNewChart("normal", ChartType.XY_LINE)
		.config().withXScale(-1F, 1F)
		.withXLabel("x")
		.withYLabel("gauss")
		.withYScale(0F, 1F)
		.done()
		.withSeries(SeriesBuilder.range(-1D, 1D, 1000))
		.bind(X, t -> t)
		.bind(Y, GaussianCDF.fn(0, 1))
		.done()
		.render();
	}

	@Test
	public void testSeries() {
		SeriesBuilder.range(-1D, 1D, 1000).forEach(System.out::println);
	}
	
	@Test
	public void testCoords() {
		SeriesBuilder.grid(-1D, 1D, -1D, 1D, 50).forEach(System.out::println);
	}
	
	@Test
	public void plotKumaraswarmy() {
		
	}
}
