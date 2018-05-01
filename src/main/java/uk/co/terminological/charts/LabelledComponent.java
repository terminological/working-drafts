package uk.co.terminological.charts;

public interface LabelledComponent {

	String getLabel();
	Boolean isHidden();
	void hide();
	
	default boolean isDisplayed() {return isHidden()==null?false:!isHidden();}
	default String getDisplayText() {return getLabel();}
}
