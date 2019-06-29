package uk.co.terminological.costbenefit;

import uk.co.terminological.costbenefit.ClassifierModel.Kumaraswamy;

public interface CostModel {
	Double tpValue(); 
	Double tnValue();
	Double fpCost();
	Double fnCost();
	
	default Kumaraswamy minimumNeeded() {
		
	}
}