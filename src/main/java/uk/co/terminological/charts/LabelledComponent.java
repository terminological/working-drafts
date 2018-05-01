package uk.co.terminological.charts;

public interface LabelledComponent {

	String getLabel();
	default String getDisplayText() {return getLabel();}
	
}
