package uk.co.terminological.costbenefit;

import java.util.stream.Stream;

import uk.co.terminological.simplechart.SeriesBuilder;

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
		
		
		
		public Kumaraswamy(Double modePos, Double modeNeg, Double spreadPos, Double spreadNeg, Double prevalence) {
			super(prevalence);
			if (!(modePos > 0 && modePos < 1 &&
					modeNeg > 0 && modeNeg < 1 &&
					spreadPos > 0 && 
					spreadNeg > 0 && 
					modePos > modeNeg)) throw new ConstraintViolationException("Modes must be between 0 and 1, spread must be greater than zero, modePos must be larger than modeNeg");
			aPos = KumaraswamyCDF.a(spreadPos, modePos);
			bPos = KumaraswamyCDF.b(spreadPos, modePos);
			aNeg = KumaraswamyCDF.a(spreadNeg, modeNeg);
			bNeg = KumaraswamyCDF.b(spreadNeg, modeNeg);
		}
		
		public ConfusionMatrix2D matrix(Double cutoff) {
			
			Double cdfPos = KumaraswamyCDF.cdf(aPos,bPos).apply(cutoff);
			Double cdfNeg = KumaraswamyCDF.cdf(aNeg,bNeg).apply(cutoff);
			
			Double eTp = prev*(1 - cdfPos);
			Double eTn = (1-prev)*cdfNeg;
			Double eFp = prev*cdfPos;
			Double eFn = (1-prev)*1-cdfPos;
			
			//TODO: prevalence is independent of cutoff - this should be correct. Classifier model accounts for prevalence by.
			
			return new ConfusionMatrix2D(eTp,eTn,eFp,eFn);
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
	
	public static class ParameterSpace {
		public ParameterSpace(ParameterSet defaults) {
			this.prevalence = Stream.of(defaults.prevalence);
			this.centralityIfPositive = Stream.of(defaults.centralityIfPositive);
			this.spreadIfPositive = Stream.of(defaults.spreadIfPositive);
			this.centralityIfNegative = Stream.of(defaults.centralityIfNegative);
			this.spreadIfNegative = Stream.of(defaults.spreadIfNegative);
			this.tpValue = Stream.of(defaults.tpValue);
			this.tnValue = Stream.of(defaults.tnValue);
			this.fpCost = Stream.of(defaults.fpCost);
			this.fnCost = Stream.of(defaults.fnCost);
			this.cutOff = Stream.of(defaults.cutOff);
		}



		Stream<Double> prevalence;
		Stream<Double> centralityIfPositive;
		Stream<Double> spreadIfPositive;
		Stream<Double> centralityIfNegative;
		Stream<Double> spreadIfNegative;
		Stream<Double> tpValue;
		Stream<Double> tnValue;
		Stream<Double> fpCost;
		Stream<Double> fnCost;
		Stream<Double> cutOff;
		
		
		
		public Stream<ParameterSet> stream() {
			ParameterSet tmp = new ParameterSet();
			return prevalence.flatMap(p -> {
				tmp.prevalence = p;
				return centralityIfPositive;
			}).flatMap(cPos -> {
				tmp.centralityIfPositive = cPos;
				return spreadIfPositive;
			}).flatMap(sPos -> {
				tmp.spreadIfPositive = sPos;
				return centralityIfNegative;
			}).flatMap(cNeg -> {
				tmp.centralityIfNegative = cNeg;
				return spreadIfNegative;
			}).flatMap(sNeg -> {
				tmp.spreadIfNegative = sNeg;
				return tpValue;
			}).flatMap(tpVal -> {
				tmp.tpValue = tpVal;
				return tnValue;
			}).flatMap(tnVal -> {
				tmp.tnValue = tnVal;
				return fpCost;
			}).flatMap(fpC -> {
				tmp.fpCost = fpC;
				return fnCost;
			}).flatMap(fnC -> {
				tmp.fnCost = fnC;
				return cutOff;
			}).map(co -> {
				tmp.cutOff = co;
				return tmp.clone();
			});
		}
	}
	
	public static interface ClassifierConfig {
		Double centralityIfPositive(); 
		Double spreadIfPositive();
		Double centralityIfNegative(); 
		Double spreadIfNegative();
	}
	
	public static interface CostModel {
		Double tpValue(); 
		Double tnValue();
		Double fpCost();
		Double fnCost();
	}
	
	public static class ParameterSet {
		public ParameterSet() {}
		public ParameterSet(Double prevalence, ClassifierConfig classifier, CostModel cost, Double cutOff) {
			
			this.prevalence = prevalence;
			this.centralityIfPositive = classifier.centralityIfPositive();
			this.spreadIfPositive = classifier.spreadIfPositive();
			this.centralityIfNegative = classifier.centralityIfNegative();
			this.spreadIfNegative = classifier.spreadIfNegative();
			this.tpValue = cost.tpValue();
			this.tnValue = cost.tnValue();
			this.fpCost = cost.fpCost();
			this.fnCost = cost.fnCost();
			this.cutOff = cutOff;
		}

		Double prevalence;
		Double centralityIfPositive;
		Double spreadIfPositive;
		Double centralityIfNegative;
		Double spreadIfNegative;
		Double tpValue;
		Double tnValue;
		Double fpCost;
		Double fnCost;
		Double cutOff;
		
		public ParameterSet clone() {
			return new ParameterSet(prevalence, centralityIfPositive, spreadIfPositive, centralityIfNegative, 
					spreadIfNegative, tpValue, tnValue, fpCost, fnCost, cutOff);
		}
	}
}
