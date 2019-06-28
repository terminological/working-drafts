package uk.co.terminological.costbenefit;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import uk.co.terminological.costbenefit.ClassifierModel.ParameterSet;

public class ParameterSpace {
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