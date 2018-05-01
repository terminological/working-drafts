package uk.co.terminological.charts;

import java.util.List;

/**
 * A scale is a range of numbers that 
 * @author rc538
 *
 */
public interface Scale extends LabelledComponent {

	String getLabel();
	String getUnit();
	Dimension getDimension();
	
	default String getDisplayText() {
		if (getLabel() == null) return (getUnit() == null) ? "" : getUnit();
		return getLabel()+ (getUnit() == null ? "":" ("+getUnit()+")");
	}
	
	public static interface Continuous extends Scale {
		Double getMinimum();
		Double getMaximum();
		Double getInterval();
	}
	
	public static interface Discrete<X> extends Scale {
		List<Orderable<X>> getCategories();
	}
}
