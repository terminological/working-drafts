package uk.co.terminological.costbenefit;

public enum CostModelEnum implements CostModel {
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