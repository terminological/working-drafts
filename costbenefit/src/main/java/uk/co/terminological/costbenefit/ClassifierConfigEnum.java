package uk.co.terminological.costbenefit;

public enum ClassifierConfigEnum implements ClassifierConfig {
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