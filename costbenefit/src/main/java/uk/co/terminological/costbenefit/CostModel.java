package uk.co.terminological.costbenefit;

public interface CostModel {
	Double tpValue(); 
	Double tnValue();
	Double fpCost();
	Double fnCost();
}