package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uk.co.terminological.simplechart.Chart.Dimension;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.Interpolation;
import uk.co.terminological.simplechart.Interpolator;
import uk.co.terminological.simplechart.Series;
import uk.co.terminological.simplechart.SeriesBuilder;
import uk.co.terminological.simplechart.SeriesBuilder.Range;
import uk.co.terminological.simplechart.aesthetics.Factory;
import uk.co.terminological.simplechart.aesthetics.XYZwithDiff;

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
			figures.withNewChart(c+" pdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("density")
			.withYScale(0F, 10F)
			.done()
			.withSeries(SeriesBuilder.range(xRange)).withColourScheme(ColourScheme.Dark2)
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
			figures.withNewChart(c+" cdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("cumulative density")
			.withYScale(0F, 1F)
			.done()
			.withSeries(SeriesBuilder.range(xRange)).withColourScheme(ColourScheme.Dark2)
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
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 1000).collect(Collectors.toList());
			figures.withNewChart(c+" roc", ChartType.XY_MULTI_LINE)
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
			ParameterSpace space2 = new ParameterSpace(defaults);
			space2.cutOff = SeriesBuilder.range(0.0, 1.0, 1000).collect(Collectors.toList());
			figures.withNewChart(c.name()+" pr", ChartType.XY_MULTI_LINE)
				.config().withXScale(0F, 1F)
				.withXLabel("precision")
				.withYLabel("recall")
				.withYScale(0F, 1F)
				.done()
				.withSeries(space2.stream()).withColourScheme(ColourScheme.Dark2)
				.bind(X, t -> t.matrix().precision())
				.bind(Y, t -> t.matrix().recall())
				.done()
				.render();
		});
	}
	
	@Test
	public void plotValue() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
			//	CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c+" "+cm+" value", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("prevalence")
					.withLabel(Z, "value")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Greens)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().relativeValue(cm,t.prevalence))
					.bind(Y, t -> t.prevalence,"prevalence")
					.done()
					.render();
				});
		});
	}
	
	/**
	 * N.B. accuracy is independent of condition
	 */
	@Test
	public void plotAccuracy() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			//ClassifierConfigEnum c = ClassifierConfigEnum.MID_INFORMATION;
			//Stream.of(CostModelEnum.values()).forEach( cm-> {
			CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
				ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
				ParameterSpace space = new ParameterSpace(defaults);
				space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
				space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
				figures.withNewChart(c+" accuracy", ChartType.XYZ_HEATMAP)
						.config().withXScale(0F, 1F)
						.withXLabel("cutoff")
						.withYLabel("prevalence")
						.withLabel(Z, "accuracy")
						.withYScale(0F, 1F)
						.done()
						.withSeries(space.stream()).withColourScheme(ColourScheme.Blues)
						.bind(X, t -> t.cutOff)
						//.bind(Y, t -> t.matrix().tp,"tp")
						//.bind(Y, t -> t.matrix().tn,"tn")
						//.bind(Y, t -> t.matrix().fp,"fp")
						//.bind(Y, t -> t.matrix().fn,"fn")
						.bind(Z, t -> t.matrix().accuracy())
						.bind(Y, t -> t.prevalence,"prevalence")
						.done()
						.render();
			//});
		});
	}
	
	@Test
	public void plotValueVersusAccuracy() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c+" "+cm+" value vs accuracy", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("prevalence")
					.withLabel(Z, "value-\\naccuracy")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.BuGn)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().relativeValue(cm,t.prevalence)-t.matrix().accuracy())
					.bind(Y, t -> t.prevalence,"prevalence")
					.done()
					.render();
			});
		});
	}
	
	@Test
	public void plotBestCutoff() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			//space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c+" "+cm+" best cutoff", ChartType.XY_MULTI_LINE)
					.config().withXScale(0F, 1F)
					.withXLabel("prevalence")
					.withYLabel("best cutoff")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
					.bind(X, t -> t.prevalence)
					.bind(Y, t -> t.model().bestCutoff(m -> m.relativeValue(cm, t.prevalence)).getFirst(),"rel value")
					.bind(Y, t -> t.model().bestCutoff(m -> m.accuracy()).getFirst(),"accuracy")
					.bind(Y, t -> t.model().bestCutoff(m -> m.f1Score()).getFirst(),"f1 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.fScore(4)).getFirst(),"f4 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.fScore(0.25)).getFirst(),"f0.25 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.matthewsCorrelationCoefficient()).getFirst(),"mcc")
					.bind(Y, t -> t.model().bestCutoff(m -> m.youdensJ()).getFirst(),"youdens")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done()
					.render();
			});
		});
	}
	
	@Test
	public void plotValueAtBestCutoff() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			//space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c+" "+cm+" statistic at best cutoff", ChartType.XY_MULTI_LINE)
					.config().withXScale(0F, 1F)
					.withXLabel("prevalence")
					.withYLabel("best cutoff")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
					.bind(X, t -> t.prevalence)
					.bind(Y, t -> t.model().bestCutoff(m -> m.relativeValue(cm, t.prevalence)).getSecond(),"rel value")
					.bind(Y, t -> t.model().bestCutoff(m -> m.accuracy()).getSecond(),"accuracy")
					.bind(Y, t -> t.model().bestCutoff(m -> m.f1Score()).getSecond(),"f1 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.fScore(4)).getSecond(),"f4 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.fScore(0.25)).getSecond(),"f0.25 score")
					.bind(Y, t -> t.model().bestCutoff(m -> m.matthewsCorrelationCoefficient()).getSecond(),"mcc")
					.bind(Y, t -> t.model().bestCutoff(m -> m.youdensJ()).getSecond(),"youdens")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done()
					.render();
			});
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void plotDebug2() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
				ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
				ParameterSpace space = new ParameterSpace(defaults);
				space.cutOff = SeriesBuilder.range(0.005, 0.995, 0.01).collect(Collectors.toList());
				space.prevalence = SeriesBuilder.range(0.005,0.995, 0.01).collect(Collectors.toList());
				
				Interpolation<ParameterSet> interp = Interpolator.fromStream(
						space.stream(),
						t -> t.matrix().relativeValue(cm,t.prevalence),
						t -> t.cutOff,
						t -> t.prevalence
						);
				
				
				Stream<XYZwithDiff> coords = SeriesBuilder.grid(Range.of(0.005, 0.995, 0.05),Range.of(0.005, 0.995, 0.05))
					.map(gxy -> Factory.Mutable.createXYZwithDiff()
							.withX(gxy.getX())
							.withY(gxy.getY()))
					.map(interp.streamAugmenter(
							(xzydiff,z) -> xzydiff.withZ(z),
							(xzydiff,dz) -> xzydiff.withDzDx(dz.get(0)).withDzDy(dz.get(1)),
							xzydiff -> xzydiff.getX(),
							xzydiff -> xzydiff.getY()
							))
					;
				
				
				figures.withNewChart(c+" "+cm+" slope", ChartType.XY_DXDY_VECTOR)
						.config().withXScale(0D, 1D)
						.withXLabel("cutoff")
						.withYLabel("prevalence")
						.withLabel(Z, "value")
						.withYScale(0D, 1D)
						.done()
						.withSeries(coords).withColourScheme(ColourScheme.BuGn)
						.bind(X, t -> t.getX(),"cutoff")
						.bind(Y, t -> t.getY(),"prevalence")
						.bind(Z, t -> t.getZ(),"value")
						.bind(DX, t -> t.getDzDx(),"dz/dx")
						.bind(DY, t -> t.getDzDy(),"dz/dy")
						
						.done()
						.render();
					});
				});
	}
}
