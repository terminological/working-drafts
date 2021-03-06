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
		Range.of(0.2D,0.8D, 4).forEach(divergence -> {
			Range.of(-0.5, 0.5, 3).forEach(skew -> {
				Kumaraswamy model = new Kumaraswamy(divergence,skew);
				model.name = "$d="+twoDp.format(divergence)+"$ $s="+twoDp.format(skew)+"$"; //+" auroc:"+twoDp.format(model.AUROC());
				model.plotPdf(pdfs);
				model.plotCdf(cdfs);
				model.plotRoc(rocs);
				model.plotPR(prs);
			});
		});
		//TODO: adlter classifier config enum to use spread and divergence
		//TODO: adlter classifier config enum to use spread and divergence
		pdfs.render(3, true, true);
		cdfs.render(3, true, true);
		rocs.render(3, true, true);
		prs.render(3, true,true);
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
				Range.of(0.2D, 0.8D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
		).map( c-> 
			Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond()))
		);	
		
		figures.withNewChart("AUROC", ChartType.XYZ_HEATMAP)
		.config()
		.withXScale(0.2F, 0.8F)
		.withYScale(-0.5F, 0.5F)
		.withXLabel("$div$")
		.withYLabel("$skew$")
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set1)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().AUROC(),"AUC")
		.done()
		.render();
		
		data = SeriesBuilder.grid(
				Range.of(0.2D, 0.8D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
			).map( c-> 
				Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
			);	
		
		figures.withNewChart("KL Divergence", ChartType.XYZ_HEATMAP)
		.config()
		.withXLabel("$div$")
		.withYLabel("$skew$")
		.withXScale(0.2F, 0.8F)
		.withYScale(-0.5F, 0.5F)
		.done()
		.withSeries(data).withColourScheme(ColourScheme.Set2)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird().KLDivergence(),"KL divergence")
		.done()
		.render();
		
		Figure figures2 = Figure.outputTo(path).withTitle("Lambda divergence");
		
		Stream.of(0.01,0.05,0.1,0.25,0.5,0.75,0.9,0.95,0.99).forEach(prev -> {
			Stream<Triple<Double,Double,Kumaraswamy>> data2 = SeriesBuilder.grid(
					Range.of(0.2D, 0.8D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D)
				).map( c-> 
					Triple.create(c.getFirst(), c.getSecond(), new Kumaraswamy(c.getFirst(), c.getSecond(), ""))
				);	
		
			figures2.withNewChart("$\\\\lambda$="+twoDp.format(prev), ChartType.XYZ_HEATMAP)
			.config()
			.withXLabel("$div$")
			.withYLabel("$skew$")
			.withXScale(0.2F, 0.8F)
			.withYScale(-0.5F, 0.5F)
			.done()
			.withSeries(data2).withColourScheme(ColourScheme.Set3)
			.bind(X, t -> t.getFirst())
			.bind(Y, t -> t.getSecond())
			.bind(Z, t -> t.getThird().LambdaDivergence(prev), " $\\\\lambda$ divergence")
			.done();
		});
		figures2.render(3,true,true);
		
	}
	
	
	
	@Test
	public void plotClassifierValue() {
		Figure figures = Figure.outputTo(path).withTitle("Max value");
		
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(0.01,0.1D,0.2,0.5).forEach(prev -> {		
				Stream<Coordinate> data = SeriesBuilder.grid(
						Range.of(0.2D, 0.8D, 0.01D),Range.of(-0.5D, 0.5D, 0.01D));
				figures.withNewChart(cm.nickname()+" $\\\\lambda$="+prev+"$", ChartType.XYZ_HEATMAP)
				.config().withXScale(0.2F, 0.8F)
				.withXLabel("divergence")
				.withYLabel("skew")
				.withYScale(-0.5F, 0.5F)
				.withScale(Z,-1D, 1D)
				.done()
				.withSeries(data).withColourScheme(ColourScheme.RedWhiteGreen)
				.bind(X, t -> t.getFirst())
				.bind(Y, t -> t.getSecond())
				.bind(Z, t -> new Kumaraswamy(t.getFirst(),t.getSecond()).screeningBeneficial(cm, prev),"value")
						//.bestCutoff(prev, m -> m.normalisedValue(cm)).getSecond()) //.bestCutoff(prev, 
						//matrix -> matrix.relativeValue(cm,prev)
						//).getValue())
				.done();
				
				
			});
		});
		figures.render(4,true,true);
	}
	
	@Test
	public void plotMinimumAcccuracy() {
		{
		Figure figures = Figure.outputTo(path).withTitle("Maximum value by auroc");		
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(0.01,0.1D,0.2,0.5).forEach(prev -> {		
				figures.withNewChart("$\\\\lambda="+prev+"$ "+cm.nickname(), ChartType.XY_MULTI_LINE)
				.config().withXScale(0.5F, 1F)
				//.withXLabel("auroc")
				.withXLabel("auroc")
				.withYLabel("best value")
				.withYScale(0F, 1F)
				.done()
				.withSeries(
						Range.of(0.1D, 0.8D, 0.01D).stream()
						).withColourScheme(ColourScheme.Dark2)
				.bind(X, div -> new Kumaraswamy(div,""+div).AUROC())
				
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).usefulCutoff(prev, c -> c.accuracy()).getSecond(),"acc")
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).usefulCutoff(prev, c -> c.normalisedValue(cm)).getSecond(),"norm mu")
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).usefulCutoff(prev, c -> c.f1Score()).getSecond(),"f1 score")
				//.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.relativeValue(cm,prev)).getFirst(),"rel mu")
				.done();
			});
		});
		figures.render(4,true,true);
	}
		{
		Figure figures = Figure.outputTo(path).withTitle("Operating point for maximum");		
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(0.01,0.1D,0.2,0.5).forEach(prev -> {		
				figures.withNewChart("$\\\\lambda="+prev+"$ "+cm.nickname(), ChartType.XY_MULTI_LINE)
				.config().withXScale(0.5F, 1F)
				//.withXLabel("auroc")
				.withXLabel("auroc")
				.withYLabel("cut-off")
				.withYScale(0F, 1F)
				.done()
				.withSeries(
						Range.of(0.1D, 0.8D, 0.01D).stream()
						).withColourScheme(ColourScheme.Dark2)
				.bind(X, div -> new Kumaraswamy(div,""+div).AUROC())
				
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.accuracy()).getFirst(),"acc")
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.normalisedValue(cm)).getFirst(),"norm mu")
				.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.f1Score()).getFirst(),"f1 score")
				//.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.relativeValue(cm,prev)).getFirst(),"rel mu")
				.done();
			});
		});
		figures.render(4,true,true);
		}
		{
			Figure figures = Figure.outputTo(path).withTitle("Operating point versus maximum");		
			Stream.of(CostModelEnum.values()).forEach( cm-> {
				Stream.of(0.01,0.1D,0.2,0.5).forEach(prev -> {		
					figures.withNewChart("$\\\\lambda="+prev+"$ "+cm.nickname(), ChartType.XY_MULTI_LINE)
					.config()
					.withXScale(0F, 1F)
					//.withXLabel("auroc")
					.withYLabel("cut-off")
					.withXLabel("best")
					.withYScale(0F, 1F)
					.done()
					.withSeries(
							Range.of(0.1D, 0.8D, 0.01D).stream()
							).withColourScheme(ColourScheme.Dark2)
					.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.accuracy()).getFirst(),"acc")
					.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.normalisedValue(cm)).getFirst(),"norm mu")
					.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.f1Score()).getFirst(),"f1 score")
					
					.bind(X, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.accuracy()).getSecond(),"acc")
					.bind(X, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.normalisedValue(cm)).getSecond(),"norm mu")
					.bind(X, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.f1Score()).getSecond(),"f1 score")
					//.bind(Y_LINE, div -> new Kumaraswamy(div,""+div).bestCutoff(prev, c -> c.relativeValue(cm,prev)).getFirst(),"rel mu")
					.done();
				});
			});
			figures.render(4,true,true);
			}
	}
	
	@Test
	public void plotFBeta() {
		Figure figures = Figure.outputTo(path).withTitle("F(β) scores");
		
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
				
				for (Double beta: Range.of(-3D, 3D, 7)) {
					double f = Math.pow(2, beta);
					series.bind(Y_LINE, t -> model.screeningBeneficial(cm, t), "β="+f);
					series.bind(X, t -> model.matrix(t, 0.5).fScore(f), "β="+f);
				}
				
				series.done();//.render();
				
			// });
		});
		figures.render(1,true,true);
	}
	
	
	
	@Test 
	public void classifierConfigValues() {
		String out = "\\begin{table}[]\n" + 
				"\\begin{tabular}{@{}lrrrr@{}}\n" + 
				"\\toprule\n" + 
				"Information gain & Divergence & Skew & AUROC & KL Divergence \\\\ \\midrule\n";
		out = out+ 
			Stream.of(ClassifierConfigEnum.values()).map( c-> {
				Kumaraswamy k = new Kumaraswamy(c);
				return k.name+" & "+
						c.divergence()+" & "+
						c.skew+" & "+
						twoDp.format(k.AUROC())+" & "+
						twoDp.format(k.KLDivergence());
			}).collect(Collectors.joining("\\\\\n"));
		out = out+"\\\\ \\bottomrule \n"+
				"\\end{tabular}\n" + 
				"\\end{table}";
		System.out.println(out);
	}
	
	@Test 
	public void costModelValues() {
		String out = "\\begin{table}[]\n" + 
				"\\begin{tabular}{@{}lrrrr@{}}\n" + 
				"\\toprule\n" + 
				"Condition & $\\mu_{tp}$ & $\\\\mu_{fn}$ & $\\\\mu_{fp}$ & $\\\\mu_{tn}$ \\\\ \\midrule\n";
		out = out+ 
			Stream.of(CostModelEnum.values()).map( c-> 
				c.nickname()+" & "+
						oneDp.format(c.tpValue)+" & "+
						oneDp.format(c.fnCost)+" & "+
						oneDp.format(c.fpCost)+" & "+
						oneDp.format(c.tnValue)
			).collect(Collectors.joining("\\\\\n"));
		out = out+"\\\\ \\bottomrule \n"+
				"\\end{tabular}\n" + 
				"\\end{table}";
		System.out.println(out);
	}
	@Test
	public void plotRelativeValue() {
		Figure figures = Figure.outputTo(path).withTitle("Relative value");
		Stream.of(CostModelEnum.values()).forEach( cm-> {
			Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			//	CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c.nickname()+":"+cm.nickname(), ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("$\\\\lambda$")
					.withLabel(Z, "relative\\nvalue")
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
		figures.render(3,true,true);
	}
	
	@Test
	public void plotNormalisedValue() throws IOException, TemplateException {
		Figure figures = Figure.outputTo(path).withTitle("Normalised value");
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
			figures.withNewChart(c.nickname()+":"+cm.nickname()+" rv", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("$\\\\lambda$")
					.withLabel(Z, "norm value")
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
		figures.render(3,true,true);
	}
	
	@Test
	public void plotRealValue() throws IOException, TemplateException {
		Figure figures = Figure.outputTo(path).withTitle("Value");
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
			figures.withNewChart(c.nickname()+":"+cm.nickname()+" rv", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("$\\\\lambda$")
					.withLabel(Z, "total value")
					.withYScale(0F, 1F)
					.withScale(Z,-50F, 50F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().absoluteValue(cm))
					.bind(Y, t -> t.prevalence,"prevalence")
					.done();
				});
		});
		figures.render(3,true,true);
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
			.and("mcc",m -> m.matthewsCorrelationCoefficient())
			.and("mi", conf -> conf.mi());
			//.and("dor", conf -> conf.diagnosticOdds());
		
		
		AtomicInteger i = new AtomicInteger(1);
		tmp.forEach((k,fn) -> {		
				
			Figure figures = Figure.outputTo(path).withTitle(k);
			ColourScheme cs = ColourScheme.sequential3(i.get()).contrast(0.5F);
			Range.of(0.2D,0.8D, 4).forEach( divergence -> {
				SeriesBuilder.range(-0.5D, 0.5D, 3).forEach( skew -> {
					// ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
					// ParameterSpace space = new ParameterSpace(defaults);
					Kumaraswamy model = new Kumaraswamy(divergence, skew);
					
					Stream<Coordinate> cutOffVPrevalence = SeriesBuilder.grid(
							Range.of(0.0, 1.0, 100),
							Range.of(0.0, 1.0, 100));
					figures.withNewChart("$d="+oneDp.format(divergence)+"$ $s="+oneDp.format(skew)+"$ $auc="+twoDp.format(model.AUROC())+"$", ChartType.XYZ_HEATMAP)
							.config().withXScale(0F, 1F)
							.withXLabel("cutoff")
							.withYLabel("$\\\\lambda$")
							.withLabel(Z, k)
							.withYScale(0F, 1F)
							.withScale(Z, 0, (k.equals("dor") ? 100:1))
							.done()
							.withSeries(cutOffVPrevalence).withColourScheme(cs)
							.bind(X, t -> t.getFirst())
							//.bind(Y, t -> t.matrix().tp,"tp")
							//.bind(Y, t -> t.matrix().tn,"tn")
							//.bind(Y, t -> t.matrix().fp,"fp")
							//.bind(Y, t -> t.matrix().fn,"fn")
							
							.bind(Y, t -> t.getSecond(),"$\\\\lambda$")
							.bind(Z, t -> fn.apply(model.matrix(t.getSecond(),t.getFirst())))
							.done();
				});
			});
			figures.render(3,true,true);
			i.incrementAndGet();
		});
	}
	
	@Test
	public void plotValueVersusAccuracy() {
		Figure figures = Figure.outputTo(path).withTitle("Value versus accuracy");
		
			Stream.of(CostModelEnum.values()).forEach( cm-> {
				Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
				//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c.nickname()+":"+cm.nickname()+" v-a", ChartType.XYZ_HEATMAP)
					.config().withXScale(0F, 1F)
					.withXLabel("cutoff")
					.withYLabel("$\\\\lambda$")
					.withLabel(Z, "normalised\\nvalue-\\naccuracy")
					.withYScale(0F, 1F)
					.withScale(Z,-1F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.RedWhiteGreen)
					.bind(X, t -> t.cutOff)
					//.bind(Y, t -> t.matrix().tp,"tp")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.bind(Z, t -> t.matrix().normalisedValue(cm)-t.matrix().accuracy())
					.bind(Y, t -> t.prevalence,"prevalence")
					.done();
			});
			
		});
		figures.render(3,true,true);
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
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getFirst(),"f4 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getFirst(),"f0.25 score")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getFirst(),"mcc")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getFirst(),"youdens")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.mi()).getFirst(),"mi")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done();
					
			});
		});
		figures.render(3,true,true);
	}
	
	@Test
	public void plotValueAtBestCutoff() {
		Figure figures = Figure.outputTo(path).withTitle("maximum statistic at best cutoff");
		
			Stream.of(CostModelEnum.values()).forEach( cm-> {
				Stream.of(ClassifierConfigEnum.values()).forEach( c-> {
			//CostModelEnum cm = CostModelEnum.CANCER_IS_UNTREATABLE;
			ParameterSet defaults = new ParameterSet(0.1,c,cm,null);
			ParameterSpace space = new ParameterSpace(defaults);
			//space.cutOff = SeriesBuilder.range(0.0, 1.0, 100).collect(Collectors.toList());
			space.prevalence = SeriesBuilder.range(0.005,0.995,0.01).collect(Collectors.toList());
			figures.withNewChart(c.nickname()+":"+cm.nickname()+" best", ChartType.XY_MULTI_LINE)
					.config().withXScale(0F, 1F)
					.withXLabel("prevalence")
					.withYLabel("statistic value")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Set1)
					.bind(X, t -> t.prevalence)
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.normalisedValue(cm)).getSecond(),"norm value")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getSecond(),"rel value")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.accuracy()).getSecond(),"accuracy")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.f1Score()).getSecond(),"f1 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getSecond(),"f4 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getSecond(),"f0.25 score")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getSecond(),"mcc")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.youdensJ()).getSecond(),"youdens")
					.bind(Y_LINE, t -> t.model().usefulCutoff(t.prevalence,m -> m.mi()).getSecond(),"mi")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done();
					
			});
		});
		figures.render(3,true,true);
	}
	
	@Test
	public void plotBestValueVsCutoff() {
		Figure figures = Figure.outputTo(path).withTitle("best value versus cutoff");
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
					.withXLabel("best cutoff")
					.withYLabel("best value")
					.withYScale(0F, 1F)
					.done()
					.withSeries(space.stream()).withColourScheme(ColourScheme.Dark2)
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.normalisedValue(cm)).getFirst(),"norm value")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getFirst(),"rel value")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.accuracy()).getFirst(),"accuracy")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.f1Score()).getFirst(),"f1 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getFirst(),"f4 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getFirst(),"f0.25 score")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getFirst(),"mcc")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getFirst(),"youdens")
					.bind(X, t -> t.model().bestCutoff(t.prevalence,m -> m.mi()).getFirst(),"mi")
					
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.normalisedValue(cm)).getSecond(),"norm value")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.relativeValue(cm, t.prevalence)).getSecond(),"rel value")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.accuracy()).getSecond(),"accuracy")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.f1Score()).getSecond(),"f1 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(4)).getSecond(),"f4 score")
					//.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.fScore(0.25)).getSecond(),"f0.25 score")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.matthewsCorrelationCoefficient()).getSecond(),"mcc")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.youdensJ()).getSecond(),"youdens")
					.bind(Y_LINE, t -> t.model().bestCutoff(t.prevalence,m -> m.mi()).getSecond(),"mi")
					//.bind(Y, t -> t.matrix().tn,"tn")
					//.bind(Y, t -> t.matrix().fp,"fp")
					//.bind(Y, t -> t.matrix().fn,"fn")
					.done();
					
			});
		});
		figures.render(3,true,true);
	}
	
	/*@SuppressWarnings("unchecked")
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
	}*/
}
