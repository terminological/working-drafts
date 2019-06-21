package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import freemarker.template.TemplateException;
import uk.co.terminological.costbenefit.ClassifierModel.ClassifierConfig;
import uk.co.terminological.costbenefit.ClassifierModel.ClassifierConfigEnum;
import uk.co.terminological.costbenefit.ClassifierModel.CostModelEnum;
import uk.co.terminological.costbenefit.ClassifierModel.ParameterSet;
import uk.co.terminological.costbenefit.ClassifierModel.ParameterSpace;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
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
		//Range spreadRange = Range.of(0.1D,1D, 6);
		//Range bRange = Range.of(2D, 5D, 4);
		//Range modeRange = Range.of(0.1, 0.9, 3);
		//Double mode = 0.75D;
		Range xRange = Range.of(0D, 1D, 1000);
		DecimalFormat df = new DecimalFormat("0.00"); 
		
		
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Function<Double,Double> pos = KumaraswamyCDF.pdf(
				KumaraswamyCDF.a(c.spreadIfPositive(),c.centralityIfPositive()),
				KumaraswamyCDF.b(c.spreadIfPositive(),c.centralityIfPositive()));
			Function<Double,Double> neg = KumaraswamyCDF.pdf(
				KumaraswamyCDF.a(c.spreadIfNegative(),c.centralityIfNegative()),
				KumaraswamyCDF.b(c.spreadIfNegative(),c.centralityIfNegative()));
			figures.withNewChart(c.name()+" pdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("density")
			.withYScale(0F, 10F)
			.done()
			.withSeries(SeriesBuilder.range(xRange))
			.bind(X, t -> t)
			.bind(Y, pos,"pos")
			.bind(Y, neg,"neg")
			.bind(Y, t -> 0.1*pos.apply(t)+0.9*neg.apply(t),"joint @ 10% prev")
			.done().render();
		});
		
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Function<Double,Double> pos = KumaraswamyCDF.cdf(
				KumaraswamyCDF.a(c.spreadIfPositive(),c.centralityIfPositive()),
				KumaraswamyCDF.b(c.spreadIfPositive(),c.centralityIfPositive()));
			Function<Double,Double> neg = KumaraswamyCDF.cdf(
				KumaraswamyCDF.a(c.spreadIfNegative(),c.centralityIfNegative()),
				KumaraswamyCDF.b(c.spreadIfNegative(),c.centralityIfNegative()));
			figures.withNewChart(c.name()+" cdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("cumulative density")
			.withYScale(0F, 1F)
			.done()
			.withSeries(SeriesBuilder.range(xRange))
			.bind(X, t -> t)
			.bind(Y, pos,"pos")
			.bind(Y, neg,"neg")
			.bind(Y, t -> 0.1*pos.apply(t)+0.9*neg.apply(t),"joint @ 10% prev")
			.done().render();
		});
	}
	
	@Test
	public void plotRoc() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			ParameterSet defaults = new ParameterSet(0.1,c,CostModelEnum.EARLY_STAGE_CANCER,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 1000);
			figures.withNewChart(c.name()+"roc", ChartType.XY_MULTI_LINE)
					.config().withXScale(0F, 1F)
					.withXLabel("1-sens")
					.withYLabel("spec")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
					.bind(X, t -> 1-t.matrix().sensitivity())
					.bind(Y, t -> t.matrix().specificity())
					.done()
					.render();
		});
	}
	
	@Test
	public void plotDebug() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
		ParameterSet defaults = new ParameterSet(0.1,c,CostModelEnum.EARLY_STAGE_CANCER,null);
		ParameterSpace space = new ParameterSpace(defaults);
		space.cutOff = SeriesBuilder.range(0.0, 1.0, 1000);
		figures.withNewChart(c.name()+" measures", ChartType.XY_MULTI_LINE)
				.config().withXScale(0F, 1F)
				.withXLabel("cutoff")
				.withYLabel("rates")
				.withYScale(0F, 1F)
				.done()
				.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
				.bind(X, t -> t.cutOff)
				.bind(Y, t -> t.matrix().tp,"tp")
				.bind(Y, t -> t.matrix().tn,"tn")
				.bind(Y, t -> t.matrix().fp,"fp")
				.bind(Y, t -> t.matrix().fn,"fn")
				.bind(Y, t -> t.matrix().accuracy(),"accuracy")
				.bind(Y, t -> t.matrix().sensitivity(),"sens")
				.bind(Y, t -> t.matrix().specificity(),"spec")
				.bind(Y, t -> t.matrix().relativeValue(),"value")
				.done()
				.render();
		});
	}
}
