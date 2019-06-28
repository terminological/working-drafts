package uk.co.terminological.costbenefit;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;

import uk.co.terminological.datatypes.Tuple;
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
		
		
		
		public Kumaraswamy(Double modePos, Double spreadPos, Double modeNeg, Double spreadNeg, Double prevalence) {
			super(prevalence);
			if (!(modePos > 0 && modePos < 1 &&
					modeNeg > 0 && modeNeg < 1 &&
					spreadPos > 0 && 
					spreadNeg > 0 && 
					modePos > modeNeg)) throw new ConstraintViolationException("Modes must be between 0 and 1, spread must be greater than zero, modePos must be larger than modeNeg");
			aPos = KumaraswamyCDF.a(spreadPos, modePos);
			bPos = KumaraswamyCDF.b(spreadPos, modePos);
			aNeg = KumaraswamyCDF.a(spreadNeg, 1-modeNeg);
			bNeg = KumaraswamyCDF.b(spreadNeg, 1-modeNeg);
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
			
			Double cdfPos = KumaraswamyCDF.cdf(aPos,bPos).apply(cutoff);
			Double cdfNeg = 1-KumaraswamyCDF.cdf(aNeg,bNeg).apply(cutoff);
			
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
			Function<Double,Double> p = KumaraswamyCDF.pdf(aPos,bPos);
			Function<Double,Double> q = x -> 1-KumaraswamyCDF.pdf(aNeg,bNeg).apply(x);
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
