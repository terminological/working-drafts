package uk.co.terminological.costbenefit;

public enum ClassifierConfigEnum implements ClassifierConfig {
	HIGH_INFORMATION(0.6,0.0,"hi"),
	MID_INFORMATION(0.4,0.0,"mid"),
	LOW_INFORMATION(0.2,0.0,"lo"),
	;
	
	private ClassifierConfigEnum(Double divergence, Double skew, String nickname) {
		this.divergence = divergence;
		this.skew = skew; 
		this.nickname = nickname;
	}
	
	Double divergence;
	Double skew;
	String nickname;
	
	@Override public Double divergence() {return divergence;}
	@Override public Double skew() {return skew;}
	
	public String toString() { return this.name().toLowerCase().replace("_", " "); }
	public String nickname() {return nickname;}
	
	
}