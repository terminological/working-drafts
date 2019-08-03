package uk.co.terminological.costbenefit;

public class ParameterSet {
	public ParameterSet(Double prevalence, Double divergence, Double skew, 
			Double tpValue, Double tnValue, Double fpCost,
			Double fnCost, Double cutOff, String name) {
		this.prevalence = prevalence;
		this.divergence = divergence;
		this.skew = skew;
		this.tpValue = tpValue;
		this.tnValue = tnValue;
		this.fpCost = fpCost;
		this.fnCost = fnCost;
		this.cutOff = cutOff;
		this.name = name;
	}
	public ParameterSet() {}
	public ParameterSet(Double prevalence, ClassifierConfig classifier, CostModel cost, Double cutOff) {
		this(prevalence,
				classifier.divergence(),
				classifier.skew(),
				cost.tpValue(),
				cost.tnValue(),
				cost.fpCost(),
				cost.fnCost(),
				cutOff,
				classifier.toString());
	}

	
	Double prevalence;
	Double divergence;
	Double skew;
	Double tpValue;
	Double tnValue;
	Double fpCost;
	Double fnCost;
	Double cutOff;
	String name;
	
	public ParameterSet clone() {
		return new ParameterSet(prevalence, divergence, skew,  
				tpValue, tnValue, fpCost, fnCost, cutOff, name);
	}
	
	public Kumaraswamy model() {
		return new Kumaraswamy(divergence, skew, name);
	}
	
	public ConfusionMatrix2D matrix() {
		return model().matrix(prevalence,cutOff);
	}
}