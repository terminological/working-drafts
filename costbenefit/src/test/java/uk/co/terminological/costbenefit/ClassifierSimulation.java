package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.Series;
import uk.co.terminological.simplechart.SeriesBuilder;
import uk.co.terminological.simplechart.SeriesBuilder.Range;

public class ClassifierSimulation {

	
	Figure figures;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception {
		Path dir = Paths.get("/home/terminological/tmp/graph");
		Files.createDirectories(dir);
		figures = Figure.outputTo(dir);
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
		SeriesBuilder.grid(-1D, 1D, 0D, 1D, 50).forEach(System.out::println);
	}
	
	@Test
	public void testSpace() {
		SeriesBuilder.space(
				Range.of(-1.0, 1.0, 3),
				Range.of(-1D, 1D, 5),
				Range.of(-1D, 1D, 7),
				Range.of(-1D, 1D, 9)
				).map(l -> Arrays.toString(l)).forEach(System.out::println);
	}
	
	@Test
	public void plotKumaraswarmy() {
		Range aRange = Range.of(2D, 5D, 8);
		//Range bRange = Range.of(2D, 5D, 4);
		Range modeRange = Range.of(0.1, 0.9, 9);
		//Double mode = 0.75D;
		Range xRange = Range.of(0D, 1D, 100);
		DecimalFormat df = new DecimalFormat("0.00"); 
		
			
			Series<Double> tmp = figures.withNewChart("plots", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("kumaraswamy")
			.withYScale(0F, 1F)
			.done()
			.withSeries(SeriesBuilder.range(xRange))
			.bind(X, t -> t);
			SeriesBuilder.space(aRange,modeRange).forEach(arr -> {
				Double a = arr[0];
				Double b = KumaraswamyCDF.b(a, arr[1]);
				String title = "a="+df.format(a)+" mode="+df.format(arr[1]);
				tmp.bind(Y, KumaraswamyCDF.cdf(a,b),title);
			});
			tmp.done().render();
	}
}
