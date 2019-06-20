package uk.co.terminological.costbenefit;

public class ClassifierModel {

	
	
	Double prevalence = 0.2;
	
	public static class AlwaysPositive extends ClassifierModel {
		
	}
	
	public static class Kurasawamy extends ClassifierModel {
		
		Double spreadPos = 0.5;
		Double modePos = 0.9;
		Double spreadNeg = 0.8;
		Double modeNeg = 0.7;
		
	}
	
	public static class AlwaysNegative extends ClassifierModel {
		
	}
}
