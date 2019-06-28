package uk.co.terminological.costbenefit;

import uk.co.terminological.costbenefit.ClassifierModel.Kumaraswamy;

public class ParameterSet {
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
		return model().matrix(cutOff);
	}
}