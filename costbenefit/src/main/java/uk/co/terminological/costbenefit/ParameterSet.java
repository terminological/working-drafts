package uk.co.terminological.costbenefit;

import uk.co.terminological.costbenefit.ClassifierModel.Kumaraswamy;

public class ParameterSet {
	public ParameterSet(Double prevalence, Double centralityIfPositive, Double spreadIfPositive,
			Double centralityIfNegative, Double spreadIfNegative, Double tpValue, Double tnValue, Double fpCost,
			Double fnCost, Double cutOff, String name) {
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
		this.name = name;
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
				cutOff,
				classifier.toString());
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
	String name;
	
	public ParameterSet clone() {
		return new ParameterSet(prevalence, centralityIfPositive, spreadIfPositive, centralityIfNegative, 
				spreadIfNegative, tpValue, tnValue, fpCost, fnCost, cutOff, name);
	}
	
	public Kumaraswamy model() {
		return new Kumaraswamy(centralityIfPositive, spreadIfPositive, centralityIfNegative, spreadIfNegative, name);
	}
	
	public ConfusionMatrix2D matrix() {
		return model().matrix(prevalence,cutOff);
	}
}