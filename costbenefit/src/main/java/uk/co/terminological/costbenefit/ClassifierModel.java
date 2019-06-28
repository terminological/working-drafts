package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Precision;

import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.SeriesBuilder;
import uk.co.terminological.simplechart.SeriesBuilder.Range;

public abstract class ClassifierModel<X> {

	ClassifierModel(Double prevalence) {prev=prevalence;}
	
	public abstract ConfusionMatrix2D matrix(X param);
	
	Double prev = 0.2;
	
	
	public static class AlwaysPositive extends ClassifierModel<Void> {

		AlwaysPositive(Double prevalence) {
			super(prevalence);
		}

		@Override
		public ConfusionMatrix2D matrix(Void param) {
			
			Double tp = prev;
			Double tn = 0D;
			Double fp = 1-prev;
			Double fn = 0D;
			
			return new ConfusionMatrix2D(tp,tn,fp,fn);
		}
	}
	
	
	
	public static class Kumaraswamy extends ClassifierModel<Double> {
		
		Double aPos;
		Double bPos;
		Double aNeg;
		Double bNeg;
		
		Function<Double,Double> pdfGivenPositive;
		Function<Double,Double> pdfGivenNegative;
		Function<Double,Double> cdfGivenPositive;
		Function<Double,Double> cdfGivenNegative;
		
		String name;
		
		public Kumaraswamy(ClassifierConfig config, Double prevalence) {
			this(config.centralityIfPositive(), config.spreadIfPositive(), config.centralityIfNegative(), config.spreadIfNegative(), prevalence, config.toString());
		}
		
		public Kumaraswamy(Double modePos, Double spreadPos, Double modeNeg, Double spreadNeg, Double prevalence, String name) {
			
			super(prevalence);
			if (!(modePos > 0 && modePos < 1 &&
					modeNeg > 0 && modeNeg < 1 &&
					spreadPos > 0 && 
					spreadNeg > 0 && 
					modePos > modeNeg)) throw new ConstraintViolationException("Modes must be between 0 and 1, spread must be greater than zero, modePos must be larger than modeNeg");
			
			
			aPos = KumaraswamyCDF.a(spreadPos, modePos);
			bPos = KumaraswamyCDF.b(spreadPos, modePos);
			pdfGivenPositive = KumaraswamyCDF.pdf(aPos, bPos);
			cdfGivenPositive = KumaraswamyCDF.cdf(aPos, bPos);
			
			aNeg = KumaraswamyCDF.a(spreadNeg, 1-modeNeg);
			bNeg = KumaraswamyCDF.b(spreadNeg, 1-modeNeg);
			pdfGivenNegative = x -> KumaraswamyCDF.pdf(aNeg, bNeg).apply(1-x);
			cdfGivenNegative = x -> 1-KumaraswamyCDF.cdf(aNeg, bNeg).apply(1-x);
			
			this.name = name;
			
		}
		
		public void plot(Figure fig) {
			fig.withNewChart(name+" pdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("density")
			.withYScale(0F, 10F)
			.done()
			.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
			.bind(X, t -> t)
			.bind(Y, pdfGivenPositive,"pos pdf")
			.bind(Y, pdfGivenNegative,"neg pdf")
			.done().render();
			//.bind(Y, t -> prev*pdfGivenPositive.apply(t)+prev*pdfGivenNegative.apply(t),"joint pdf")
			
			fig.withNewChart(name+" cdf", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("x")
			.withYLabel("cumulative")
			.withYScale(0F, 1F)
			.done()
			.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
			.bind(Y, cdfGivenPositive,"pos cdf")
			.bind(Y, cdfGivenNegative,"neg cdf")
			//.bind(Y, t -> prev*cdfGivenPositive.apply(t)+prev*cdfGivenNegative.apply(t),"joint cdf")
			.done().render();
		
			fig.withNewChart(name+" roc", ChartType.XY_MULTI_LINE)
			.config().withXScale(0F, 1F)
			.withXLabel("1-sens")
			.withYLabel("spec")
			.withYScale(0F, 1F)
			.done()
			.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
			.bind(X, t -> 1-matrix(t).sensitivity())
			.bind(Y, t -> matrix(t).specificity())
			.done()
			.render();
	
			
		}
		
		public Tuple<Double,Double> bestCutoff(Function<ConfusionMatrix2D,Double> feature) {
			Double value = Double.MIN_VALUE;
			Double bestCutoff = Double.NaN;
			for (Double d=0D; d<=1.0D;d += 0.001) {
				ConfusionMatrix2D tmp = matrix(d);
				if (feature.apply(tmp) > value) {
					value = feature.apply(tmp);
					bestCutoff = d;
				}
			}
			return Tuple.create(bestCutoff,value);
		}
		
		public boolean screeningBeneficial(CostModel model, Double prevalence) {
			Double best = bestCutoff(mat -> mat.relativeValue(model, prevalence)).getFirst();
			return !Precision.equals(best, 0.0D) && !Precision.equals(best, 1.0D);
		}
		
		public ConfusionMatrix2D matrix(Double cutoff) {
			
			Double cdfPos = cdfGivenPositive.apply(cutoff);
			Double cdfNeg = cdfGivenNegative.apply(cutoff);
			
			Double eTp = prev*(1-cdfPos);
			Double eTn = (1-prev)*cdfNeg;
			Double eFn = prev*cdfPos;
			Double eFp = (1-prev)*(1-cdfNeg);
			
			//TODO: prevalence is independent of cutoff - this should be correct. Classifier model accounts for prevalence by.
			
			return new ConfusionMatrix2D(eTp,eTn,eFp,eFn);
		}
		
		public Double AUROC() {
			return 
			SeriesBuilder.range(0.0, 1.0, 1000)
				.map(c -> matrix(c))
				.map(m -> Tuple.create(m.sensitivity(), m.specificity()))
				.collect(TrapeziodIntegrator.integrator());
		}
		
		public Double KLDivergence() {
			Function<Double,Double> p = pdfGivenPositive;
			Function<Double,Double> q = pdfGivenNegative;
			Double dpq = 
					SeriesBuilder.range(0.0, 1.0, 1000)
						.map(x -> Tuple.create(x,p.apply(x)*Math.log(p.apply(x)/q.apply(x))))
						.collect(TrapeziodIntegrator.integrator());
			Double dqp = 
					SeriesBuilder.range(0.0, 1.0, 1000)
						.map(x -> Tuple.create(x,q.apply(x)*Math.log(q.apply(x)/p.apply(x))))
						.collect(TrapeziodIntegrator.integrator());
			return dpq+dqp;		
		}
		
	}
	
	public static class AlwaysNegative extends ClassifierModel<Void> {

		AlwaysNegative(Double prevalence) {
			super(prevalence);
		}
		
		
		
		@Override
		public ConfusionMatrix2D matrix(Void param) {
			
			Double tp = 0D;
			Double tn = 1-prev;
			Double fp = 0D;
			Double fn = prev;
			
			return new ConfusionMatrix2D(tp,tn,fp,fn);
		}
	}
	
	
}
