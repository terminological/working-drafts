package uk.co.terminological.costbenefit;

public interface ClassifierConfig {
	Double centralityIfPositive(); 
	Double spreadIfPositive();
	Double centralityIfNegative(); 
	Double spreadIfNegative();
}