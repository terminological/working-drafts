package uk.co.terminological.costbenefit;

public enum CostModelEnum implements CostModel {
	EARLY_STAGE_CANCER(10.0,1.0,-0.1,-100.0,"early ca"),
	CANCER_IS_UNTREATABLE(2.0,10.0,-10.0,-0.5,"late ca"),
	DIABETES(4.0,0.1,-1.0,-0.1,"diabetes"),
	SEPSIS(10.0,0.0,0.0,-5.0,"sepsis"),
	EARLY_STAGE_DEMENTIA(2.0,5.0,-2.0,-1.0,"dementia"),
	NON_ACCIDENTAL_INJURY(100.0,1.0,-20.0,-20.0,"nai"),
	IMMINENT_END_OF_LIFE(2.0,0.0,-1.0,-0.1,"eol"),
	ENDOSCOPY_UNINFORMATIVE(2.0,0.1,-1.0,-2.0,"no scope")
	;
	
	private String nickname;

	private CostModelEnum(Double tpValue, Double tnValue, Double fpCost, Double fnCost, String nickname) {
		this.tpValue = tpValue;
		this.tnValue = tnValue;
		this.fpCost = fpCost;
		this.fnCost = fnCost;
		this.nickname = nickname;
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
	public String nickname() {return nickname;}
	
}