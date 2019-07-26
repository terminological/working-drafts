package uk.co.terminological.costbenefit;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ParameterSpace {
	public ParameterSpace(ParameterSet defaults) {
		this.prevalence = Collections.singletonList(defaults.prevalence);
		this.divergence = Collections.singletonList(defaults.divergence);
		this.skew = Collections.singletonList(defaults.skew);
		this.tpValue = Collections.singletonList(defaults.tpValue);
		this.tnValue = Collections.singletonList(defaults.tnValue);
		this.fpCost = Collections.singletonList(defaults.fpCost);
		this.fnCost = Collections.singletonList(defaults.fnCost);
		this.cutOff = Collections.singletonList(defaults.cutOff);
	}



	List<Double> prevalence;
	List<Double> divergence;
	List<Double> skew;
	List<Double> tpValue;
	List<Double> tnValue;
	List<Double> fpCost;
	List<Double> fnCost;
	List<Double> cutOff;
	
	
	
	public Stream<ParameterSet> stream() {
		ParameterSet tmp = new ParameterSet();
		return prevalence.stream().flatMap(p -> {
			tmp.prevalence = p;
			return divergence.stream(); //TODO: gets consumed.
		}).flatMap(div -> {
			tmp.divergence = div;
			return skew.stream();
		}).flatMap(skew -> {
			tmp.skew = skew;
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