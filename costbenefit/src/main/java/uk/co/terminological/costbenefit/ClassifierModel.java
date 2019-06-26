package uk.co.terminological.costbenefit;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
			aNeg = KumaraswamyCDF.a(spreadNeg, modeNeg);
			bNeg = KumaraswamyCDF.b(spreadNeg, modeNeg);
		}
		
		public Double bestCutoff(Function<ConfusionMatrix2D,Double> feature) {
			Double value = Double.MIN_VALUE;
			Double bestCutoff = Double.NaN;
			for (Double d=0D; d<1.0D;d += 0.01) {
				ConfusionMatrix2D tmp = matrix(d);
				if (feature.apply(tmp) > value) {
					value = feature.apply(tmp);
					bestCutoff = d;
				}
			}
			return bestCutoff;
		}
		
		public boolean screeningBeneficial(CostModel model, Double prevalence) {
			Double best = bestCutoff(mat -> mat.withCostModel(model, prevalence).relativeValue());
			return !Precision.equals(best, 0.0D) && !Precision.equals(best, 1.0D);
		}
		
		public ConfusionMatrix2D matrix(Double cutoff) {
			
			Double cdfPos = KumaraswamyCDF.cdf(aPos,bPos).apply(cutoff);
			Double cdfNeg = KumaraswamyCDF.cdf(aNeg,bNeg).apply(cutoff);
			
			Double eTp = prev*(1-cdfPos);
			Double eTn = (1-prev)*cdfNeg;
			Double eFn = prev*cdfPos;
			Double eFp = (1-prev)*(1-cdfNeg);
			
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
			this.prevalence = Collections.singletonList(defaults.prevalence);
			this.centralityIfPositive = Collections.singletonList(defaults.centralityIfPositive);
			this.spreadIfPositive = Collections.singletonList(defaults.spreadIfPositive);
			this.centralityIfNegative = Collections.singletonList(defaults.centralityIfNegative);
			this.spreadIfNegative = Collections.singletonList(defaults.spreadIfNegative);
			this.tpValue = Collections.singletonList(defaults.tpValue);
			this.tnValue = Collections.singletonList(defaults.tnValue);
			this.fpCost = Collections.singletonList(defaults.fpCost);
			this.fnCost = Collections.singletonList(defaults.fnCost);
			this.cutOff = Collections.singletonList(defaults.cutOff);
		}



		List<Double> prevalence;
		List<Double> centralityIfPositive;
		List<Double> spreadIfPositive;
		List<Double> centralityIfNegative;
		List<Double> spreadIfNegative;
		List<Double> tpValue;
		List<Double> tnValue;
		List<Double> fpCost;
		List<Double> fnCost;
		List<Double> cutOff;
		
		
		
		public Stream<ParameterSet> stream() {
			ParameterSet tmp = new ParameterSet();
			return prevalence.stream().flatMap(p -> {
				tmp.prevalence = p;
				return centralityIfPositive.stream(); //TODO: gets consumed.
			}).flatMap(cPos -> {
				tmp.centralityIfPositive = cPos;
				return spreadIfPositive.stream();
			}).flatMap(sPos -> {
				tmp.spreadIfPositive = sPos;
				return centralityIfNegative.stream();
			}).flatMap(cNeg -> {
				tmp.centralityIfNegative = cNeg;
				return spreadIfNegative.stream();
			}).flatMap(sNeg -> {
				tmp.spreadIfNegative = sNeg;
				return tpValue.stream();
			}).flatMap(tpVal -> {
				tmp.tpValue = tpVal;
				return tnValue.stream();
			}).flatMap(tnVal -> {
				tmp.tnValue = tnVal;
				return fpCost.stream();
			}).flatMap(fpC -> {
				tmp.fpCost = fpC;
				return fnCost.stream();
			}).flatMap(fnC -> {
				tmp.fnCost = fnC;
				return cutOff.stream();
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
	
	public static enum ClassifierConfigEnum implements ClassifierConfig {
		HIGH_INFORMATION(0.8,0.1,0.2,0.1),
		MID_INFORMATION(0.7,0.3,0.3,0.3),
		LOW_INFORMATION(0.6,0.5,0.4,0.5),
		;
		
		private ClassifierConfigEnum(Double centralityIfPositive, Double spreadIfPositive, Double centralityIfNegative,	Double spreadIfNegative) {
			this.centralityIfPositive = centralityIfPositive;
			this.spreadIfPositive = spreadIfPositive;
			this.centralityIfNegative = centralityIfNegative;
			this.spreadIfNegative = spreadIfNegative;
		}
		
		Double centralityIfPositive;
		Double spreadIfPositive;
		Double centralityIfNegative;
		Double spreadIfNegative;
		
		@Override public Double centralityIfPositive() {return centralityIfPositive;}
		@Override public Double spreadIfPositive() {return spreadIfPositive;}
		@Override public Double centralityIfNegative() {return centralityIfNegative;}
		@Override public Double spreadIfNegative() {return centralityIfNegative;}
		
		public String toString() { return this.name().toLowerCase().replace("_", " "); }
		
	}
	
	public static interface CostModel {
		Double tpValue(); 
		Double tnValue();
		Double fpCost();
		Double fnCost();
	}
	
	public static enum CostModelEnum implements CostModel {
		EARLY_STAGE_CANCER(10.0,1.0,-0.1,-100.0),
		CANCER_IS_UNTREATABLE(2.0,10.0,-10.0,-0.5),
		DIABETES(4.0,0.1,-1.0,-0.1),
		SEPSIS(10.0,0.0,0.0,-5.0),
		EARLY_STAGE_DEMENTIA(2.0,5.0,-2.0,-1.0),
		NON_ACCIDENTAL_INJURY(100.0,1.0,-20.0,-20.0),
		IMMINENT_END_OF_LIFE(2.0,0.0,-1.0,-0.1),
		ENDOSCOPY_UNINFORMATIVE(2.0,0.1,-1.0,-2.0)
		;
		
		private CostModelEnum(Double tpValue, Double tnValue, Double fpCost, Double fnCost) {
			this.tpValue = tpValue;
			this.tnValue = tnValue;
			this.fpCost = fpCost;
			this.fnCost = fnCost;
		}
		
		Double tpValue;
		Double tnValue;
		Double fpCost;
		Double fnCost;
		
		@Override public Double tpValue() {return tpValue;}
		@Override public Double tnValue() {return tnValue;}
		@Override public Double fpCost() {return fpCost;}
		@Override public Double fnCost() {return fpCost;}
		
		public String toString() { return this.name().toLowerCase().replace("_", " "); }
		
	}
	
	public static class ParameterSet {
		public ParameterSet(Double prevalence, Double centralityIfPositive, Double spreadIfPositive,
				Double centralityIfNegative, Double spreadIfNegative, Double tpValue, Double tnValue, Double fpCost,
				Double fnCost, Double cutOff) {
			this.prevalence = prevalence;
			this.centralityIfPositive = centralityIfPositive;
			this.spreadIfPositive = spreadIfPositive;
			this.centralityIfNegative = centralityIfNegative;
			this.spreadIfNegative = spreadIfNegative;
			this.tpValue = tpValue;
			this.tnValue = tnValue;
			this.fpCost = fpCost;
			this.fnCost = fnCost;
			this.cutOff = cutOff;
		}
		public ParameterSet() {}
		public ParameterSet(Double prevalence, ClassifierConfig classifier, CostModel cost, Double cutOff) {
			this(prevalence,
					classifier.centralityIfPositive(),
					classifier.spreadIfPositive(),
					classifier.centralityIfNegative(),
					classifier.spreadIfNegative(),
					cost.tpValue(),
					cost.tnValue(),
					cost.fpCost(),
					cost.fnCost(),
					cutOff);
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
		
		public Kumaraswamy model() {
			return new Kumaraswamy(centralityIfPositive, spreadIfPositive, centralityIfNegative, spreadIfNegative, prevalence);
		}
		
		public ConfusionMatrix2D matrix() {
			return model().matrix(cutOff).withCostModel(tpValue, tnValue, fpCost, fnCost, prevalence);
		}
	}
	
	
}
