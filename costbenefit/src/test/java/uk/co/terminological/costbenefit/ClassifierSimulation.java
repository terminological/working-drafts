package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import freemarker.template.TemplateException;
import uk.co.terminological.costbenefit.ClassifierModel.Kumaraswamy;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Coordinate;
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
		//Path dir = Paths.get(System.getProperty("user.home"),"Dropbox/costOptimisation/simulation");
		Path dir = Paths.get(System.getProperty("user.home"),"tmp/graph");
		Files.createDirectories(dir);
		figures = Figure.outputTo(dir);
	}

	
	
	@Test
	public void plotKumaraswarmy() {
		//Range spreadRange = Range.of(0.1D,1D, 6);
		//Range bRange = Range.of(2D, 5D, 4);
		//Range modeRange = Range.of(0.1, 0.9, 3);
		//Double mode = 0.75D;
		/*Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Kumaraswamy model = new Kumaraswamy(c);
			model.plot(figures);
			System.out.println(model.name);
			System.out.println("AUROC: "+model.AUROC());
			System.out.println("Dkl: "+model.KLDivergence());
			System.out.println("Dkl lambda @ 0.2: "+model.LambdaDivergence(0.2));
		});*/	
		
		new Kumaraswamy(0.65,0.5D,"spread div test").plot(figures);
	}
	
	@Test
	public void plotKumaraswarmyFeatures() {
		//Range spreadRange = Range.of(0.1D,1D, 6);
		
		Stream<Triple<Double,Double,Kumaraswamy>> data = SeriesBuilder.grid(
				Range.of(0D, 0.4D, 0.01D),Range.of(-0.25D, 0.25D, 0.01D)
		).map( c-> 
			Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
		);	
		
		figures.withNewChart("AUROC", ChartType.XYZ_HEATMAP)
		.config().withXScale(0F, 0.4F)
		.withXLabel("divergence")
		.withYLabel("skew")
		.withYScale(-0.25F, 0.25F)
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set1)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().AUROC())
		.done()
		.render();
		
		
		data = SeriesBuilder.grid(
					Range.of(0D, 0.4D, 0.01D),Range.of(-0.25D, 0.25D, 0.01D)
				).map( c-> 
					Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
				);	
		
		figures.withNewChart("Lambda Divergence", ChartType.XYZ_HEATMAP)
		.config().withXScale(0F, 0.4F)
		.withXLabel("divergence")
		.withYLabel("skew")
		.withYScale(-0.25F, 0.25F)
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set3)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().LambdaDivergence(0.01))
		.done()
		.render();
		
		data = SeriesBuilder.grid(
				Range.of(0D, 0.4D, 0.01D),Range.of(-0.25D, 0.25D, 0.01D)
			).map( c-> 
				Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
			);	
		
		figures.withNewChart("KL Divergence", ChartType.XYZ_HEATMAP)
		.config().withXScale(0F, 0.4F)
		.withXLabel("divergence")
		.withYLabel("skew")
		.withYScale(-0.25F, 0.25F)
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set2)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().KLDivergence())
		.done()
		.render();
		
	}
	
	@Test
	public void plotPR() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			ParameterSet defaults = new ParameterSet(0.1,c,CostModelEnum.EARLY_STAGE_CANCER,null);
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
	public void plotClassifierValue() {
		Double prev = 0.01D;
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			
			
			
			Stream<Coordinate> data = SeriesBuilder.grid(
					Range.of(0D, 0.4D, 0.001D),
					Range.of(-0.25D, 0.25D, 0.001D));
					
			figures.withNewChart(cm+"value at prev 0.01", ChartType.XYZ_HEATMAP)
			.config().withXScale(0F, 0.4F)
			.withXLabel("divergence")
			.withYLabel("skew")
			.withYScale(-0.25F, 0.25F)
			.done()
			.withSeries(data).withColourScheme(ColourScheme.Set3)
			.bind(X, t -> t.getFirst())
			.bind(Y, t -> t.getSecond())
			.bind(Z, t -> new Kumaraswamy(t.getFirst(),t.getSecond()).bestCutoff(prev, 
					matrix -> matrix.absoluteValue(cm)
					).getValue())
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
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
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
	
	
	@Test
	public void plotRealValue() {
		Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			Stream.of(CostModelEnum.values()).forEach( cm-> {
			//	CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c+" "+cm+" abs value", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("prevalence")
					.withLabel(Z, "value")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().normalisedValue(cm))
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
			CostModelEnum cm = CostModelEnum.DIABETES;
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
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.normalisedValue(cm)).getFirst(),"norm value")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getFirst(),"rel value")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.accuracy()).getFirst(),"accuracy")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.f1Score()).getFirst(),"f1 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getFirst(),"f4 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getFirst(),"f0.25 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getFirst(),"mcc")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getFirst(),"youdens")
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
					.withYLabel("statistic value")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Set1)
					.bind(X, t -> t.prevalence)
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.normalisedValue(cm)).getSecond(),"norm value")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getSecond(),"rel value")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.accuracy()).getSecond(),"accuracy")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.f1Score()).getSecond(),"f1 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getSecond(),"f4 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getSecond(),"f0.25 score")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getSecond(),"mcc")
					.bind(Y, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getSecond(),"youdens")
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
