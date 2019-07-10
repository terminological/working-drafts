package uk.co.terminological.costbenefit;

public enum ClassifierConfigEnum implements ClassifierConfig {
	HIGH_INFORMATION(0.8,0.2,0.2,0.2,"hi"),
	MID_INFORMATION(0.7,0.3,0.3,0.3,"mid"),
	LOW_INFORMATION(0.6,0.4,0.4,0.4,"lo"),
	;
	
	private ClassifierConfigEnum(Double centralityIfPositive, Double spreadIfPositive, Double centralityIfNegative,	Double spreadIfNegative, String nickname) {
		this.centralityIfPositive = centralityIfPositive;
		this.spreadIfPositive = spreadIfPositive;
		this.centralityIfNegative = centralityIfNegative;
		this.spreadIfNegative = spreadIfNegative;
		this.nickname = nickname;
	}
	
	Double centralityIfPositive;
	Double spreadIfPositive;
	Double centralityIfNegative;
	Double spreadIfNegative;
	String nickname;
	
	@Override public Double centralityIfPositive() {return centralityIfPositive;}
	@Override public Double spreadIfPositive() {return spreadIfPositive;}
	@Override public Double centralityIfNegative() {return centralityIfNegative;}
	@Override public Double spreadIfNegative() {return spreadIfNegative;}
	
	public String toString() { return this.name().toLowerCase().replace("_", " "); }
	public String nickname() {return nickname;}
	
	
}