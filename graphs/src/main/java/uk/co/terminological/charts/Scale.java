package uk.co.terminological.charts;

import java.util.List;

/**
 * A scale is a range of numbers that 
 * @author rc538
 *
 */
public interface Scale<X> extends LabelledComponent {

	static Continuous X_AXIS = null /* not yet implemented */;
	static Continuous Y_AXIS = null /* not yet implemented */;
	
	String getLabel();
	String getUnit();
	Dimension getDimension();
	
	default String getDisplayText() {
		if (getLabel() == null) return (getUnit() == null) ? "" : getUnit();
		return getLabel()+ (getUnit() == null ? "":" ("+getUnit()+")");
	}
	
	public static interface Continuous extends Scale<Double> {
		Double getMinimum();
		Double getMaximum();
		Double getInterval();
	}
	
	public static interface Discrete<X> extends Scale<X> {
		List<Orderable<X>> getCategories();
	}
}
