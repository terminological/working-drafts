package uk.co.terminological.charts;

import java.util.List;
import java.util.function.Function;

/**
 * Binds a plot to a (potentially interrupted) ordered data set.
 * The data set may define things about the plot e.g. min-max ranges for a given / scale or axis
 * defines how the data (or generating function) is mapped to the various scales
 * defines how the 
 * @author rc538
 *
 */
public interface Series<X> extends LabelledComponent, DataBound<X> {

	Geometry getGeometry();
	Series<X> withGeometry(Geometry geom);
	<Y> Series<X> withScale(Scale<Y> scale, @SuppressWarnings("unchecked") Function<X,Y>... mapper);
	List<Scale<?>> getScales();
	
}
