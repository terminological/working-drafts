package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
import uk.co.terminological.datatypes.FluentMap;
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

	NumberFormat twoDp = new DecimalFormat("#.##");
	NumberFormat oneDp = new DecimalFormat("#.#");
	
	Path path;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception {
		//Path dir = Paths.get(System.getProperty("user.home"),"Dropbox/costOptimisation/simulation");
		Path dir = Paths.get(System.getProperty("user.home"),"tmp/graph");
		Files.createDirectories(dir);
		path = dir;
	}

	
	
	@Test
	public void plotKumaraswarmy() {
		Figure pdfs = Figure.outputTo(path).withTitle("probability density");
		Figure cdfs = Figure.outputTo(path).withTitle("cumulative probability density");
		Figure rocs = Figure.outputTo(path).withTitle("receiver operator curves");
		Figure prs = Figure.outputTo(path).withTitle("precision recall curves");
		Range.of(0.2D,0.7D, 6).forEach(divergence -> {
			Range.of(-0.5, 0.5, 5).forEach(skew -> {
				Kumaraswamy model = new Kumaraswamy(divergence,skew);
				model.name = "d:"+twoDp.format(divergence)+" s:"+twoDp.format(skew)+" auc:"+twoDp.format(model.AUROC());
				model.plotPdf(pdfs);
				model.plotCdf(cdfs);
				model.plotRoc(rocs);
				model.plotPR(prs);
			});
		});
		//TODO: adlter classifier config enum to use spread and divergence
		//TODO: adlter classifier config enum to use spread and divergence
		pdfs.render(5, true);
		cdfs.render(5, true);
		rocs.render(5, true);
		prs.render(5, true);
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
		
		//new Kumaraswamy(0.65,0.5D,"spread div test").plot(figures);
	}
	
	//TODO: noise in thes plots ? precision / KL divergence measure maybe not working
	@Test
	public void plotKumaraswarmyFeatures() throws IOException, TemplateException {
		//Range spreadRange = Range.of(0.1D,1D, 6);
		Figure figures = Figure.outputTo(path);
		Stream<Triple<Double,Double,Kumaraswamy>> data = SeriesBuilder.grid(
				Range.of(0.2D, 0.7D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
		).map( c-> 
			Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond()))
		);	
		
		figures.withNewChart("AUROC", ChartType.XYZ_HEATMAP)
		.config()
		.withXScale(0F, 0.7F)
		.withYScale(-0.5F, 0.5F)
		.withXLabel("divergence")
		.withYLabel("skew")
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set1)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().AUROC())
		.done()
		.render();
		
		data = SeriesBuilder.grid(
				Range.of(0.2D, 0.7D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
			).map( c-> 
				Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
			);	
		
		figures.withNewChart("KL Divergence", ChartType.XYZ_HEATMAP)
		.config()
		.withXLabel("divergence")
		.withYLabel("skew")
		.withXScale(0F, 0.7F)
		.withYScale(-0.5F, 0.5F)
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set2)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().KLDivergence())
		.done()
		.render();
		
		Figure figures2 = Figure.outputTo(path).withTitle("Lambda divergence");
		
		Stream.of(0.01,0.05,0.1,0.25,0.5,0.75,0.9,0.95,0.99).forEach(prev -> {
			Stream<Triple<Double,Double,Kumaraswamy>> data2 = SeriesBuilder.grid(
					Range.of(0.2D, 0.7D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
				).map( c-> 
					Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
				);	
		
			figures2.withNewChart("p="+twoDp.format(prev), ChartType.XYZ_HEATMAP)
			.config()
			.withXLabel("divergence")
			.withYLabel("skew")
			.withXScale(0F, 0.7F)
			.withYScale(-0.5F, 0.5F)
			.done()
			.withSeries(data2).withColourScheme(ColourScheme.Set3)
			.bind(X, t -> t.getFirst())
			.bind(Y, t -> t.getSecond())
			.bind(Z, t -> t.getThird().LambdaDivergence(prev))
			.done();
		});
		figures2.render(3,true);
		
	}
	
	
	
	@Test
	public void plotClassifierValue() {
		Figure figures = Figure.outputTo(path).withTitle("Max value");
		
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(0.01,0.1D,0.2,0.5).forEach(prev -> {		
				Stream<Coordinate> data = SeriesBuilder.grid(
						Range.of(0D, 0.4D, 0.005D),
						Range.of(-0.25D, 0.25D, 0.005D));
						
				figures.withNewChart(cm.nickname()+" p="+prev, ChartType.XYZ_HEATMAP)
				.config().withXScale(0F, 0.4F)
				.withXLabel("divergence")
				.withYLabel("skew")
				.withYScale(-0.25F, 0.25F)
				.withScale(Z,-1D, 1D)
				.done()
				.withSeries(data).withColourScheme(ColourScheme.RedWhiteGreen)
				.bind(X, t -> t.getFirst())
				.bind(Y, t -> t.getSecond())
				.bind(Z, t -> new Kumaraswamy(t.getFirst(),t.getSecond()).screeningBeneficial(cm, prev))
						//.bestCutoff(prev, m -> m.normalisedValue(cm)).getSecond()) //.bestCutoff(prev, 
						//matrix -> matrix.relativeValue(cm,prev)
						//).getValue())
				.done();
				
				
			});
		});
		figures.render(4,true);
	}
	
	@Test
	public void plotFBeta() {
		Figure figures = Figure.outputTo(path).withTitle("F Beta scores");
		
		Stream.of(CostModelEnum.values()).forEach( cm-> {
					
				
				ClassifierConfig c = ClassifierConfigEnum.MID_INFORMATION;
			
				Stream<Double> data = SeriesBuilder.range(
						Range.of(0.01, 0.99, 0.01)); //prev
						
				
				Kumaraswamy model = new Kumaraswamy(c);
				
				Series<Double> series = figures.withNewChart(cm.nickname(), ChartType.XY_MULTI_LINE)
				.config().withYScale(0F, 0.5F)
				.withYLabel("max value")
				.withXLabel("f score")
				//.withYScale(-5F, 5F)
				.done()
				.withSeries(data);//.withColourScheme(ColourScheme.RedWhiteGreen)
				
				for (Double beta: Range.of(-5D, 5D, 11)) {
					double f = Math.pow(2, beta);
					series.bind(Y_LINE, t -> model.screeningBeneficial(cm, t), "beta: "+f);
					series.bind(X, t -> model.matrix(t, 0.5).fScore(f), "beta: "+f);
				}
				
				series.done();//.render();
				
			// });
		});
		figures.render(3,true);
	}
	
	@Test
	public void plotValue() {
		Figure figures = Figure.outputTo(path).withTitle("Relatuve value");
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
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
					.withScale(Z,-1F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().relativeValue(cm,t.prevalence))
					.bind(Y, t -> t.prevalence,"prevalence")
					.done();
				});
		});
		figures.render(3,true);
	}
	
	
	@Test
	public void plotRealValue() throws IOException, TemplateException {
		Figure figures = Figure.outputTo(path).withTitle("Real value");
		/*Stream.of(CostModelEnum.EARLY_STAGE_CANCER, 
				CostModelEnum.DIABETES,
				CostModelEnum.ENDOSCOPY_UNINFORMATIVE
				).forEach( cm-> {*/
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c.nickname()+":"+cm.nickname(), ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("prevalence")
					.withLabel(Z, "value")
					.withYScale(0F, 1F)
					.withScale(Z,-1F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().normalisedValue(cm))
					.bind(Y, t -> t.prevalence,"prevalence")
					.done();
				});
		});
		figures.render(3,true);
	}
	
	/**
	 * N.B. accuracy is independent of condition
	 * @throws TemplateException 
	 * @throws IOException 
	 */
	@Test
	public void plotAccuracy() throws IOException, TemplateException {
		
		FluentMap<String,Function<ConfusionMatrix2D,Double>> tmp = new FluentMap<>();
		tmp
			.and("accuracy", conf -> conf.accuracy())
			.and("f1 score", conf -> conf.f1Score())
			.and("mcc",m -> m.matthewsCorrelationCoefficient());
		
		AtomicInteger i = new AtomicInteger(1);
		tmp.forEach((k,fn) -> {		
				
			Figure figures = Figure.outputTo(path).withTitle(k);
			ColourScheme cs = ColourScheme.sequential3(i.get()).contrast(0.5F);
			SeriesBuilder.range(0D, 0.4D, 3).forEach( divergence -> {
				SeriesBuilder.range(-0.2D, 0.2D, 3).forEach( skew -> {
					// ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
					// ParameterSpace space = new ParameterSpace(defaults);
					Kumaraswamy model = new Kumaraswamy(divergence, skew);
					
					Stream<Coordinate> cutOffVPrevalence = SeriesBuilder.grid(
							Range.of(0.0, 1.0, 100),
							Range.of(0.0, 1.0, 100));
					figures.withNewChart("d="+oneDp.format(divergence)+" s="+oneDp.format(skew)+" AUC:"+twoDp.format(model.AUROC()), ChartType.XYZ_HEATMAP)
							.config().withXScale(0F, 1F)
							.withXLabel("cutoff")
							.withYLabel("prevalence")
							.withLabel(Z, k)
							.withYScale(0F, 1F)
							.withScale(Z, 0, 1)
							.done()
							.withSeries(cutOffVPrevalence).withColourScheme(cs)
							.bind(X, t -> t.getFirst())
							//.bind(Y, t -> t.matrix().tp,"tp")
							//.bind(Y, t -> t.matrix().tn,"tn")
							//.bind(Y, t -> t.matrix().fp,"fp")
							//.bind(Y, t -> t.matrix().fn,"fn")
							
							.bind(Y, t -> t.getSecond(),"prevalence")
							.bind(Z, t -> fn.apply(model.matrix(t.getSecond(),t.getFirst())))
							.done();
				});
			});
			figures.render(3,true);
			i.incrementAndGet();
		});
	}
	
	@Test
	public void plotValueVersusAccuracy() {
		Figure figures = Figure.outputTo(path);
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
					.withScale(Z,-1F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
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
		Figure figures = Figure.outputTo(path).withTitle("best cutoff");
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			//ClassifierConfigEnum c = ClassifierConfigEnum.HIGH_INFORMATION;
			
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			//space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c.nickname()+":"+cm.nickname(), ChartType.XY_MULTI_LINE)
					.config().withXScale(0F, 1F)
					.withXLabel("prevalence")
					.withYLabel("best cutoff")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
					.bind(X, t -> t.prevalence)
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.normalisedValue(cm)).getFirst(),"norm value")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getFirst(),"rel value")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.accuracy()).getFirst(),"accuracy")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.f1Score()).getFirst(),"f1 score")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getFirst(),"f4 score")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getFirst(),"f0.25 score")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getFirst(),"mcc")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getFirst(),"youdens")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done();
					
			});
		});
		figures.render(3,true);
	}
	
	@Test
	public void plotValueAtBestCutoff() {
		Figure figures = Figure.outputTo(path);
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
		Figure figures = Figure.outputTo(path);
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
