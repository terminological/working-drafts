package uk.co.terminological.charts;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A plot is a scaled 2D projection representing scales, units and labels of 2 or more dimensions.
 * Dimensions may be X, Y, Z, colour, size, shape
 * 
 * Plots have coordinate schemes.  
 * Plots define the scales axes.
 * Plots contain the ranges of continuous
 * @author rc538
 *
 */
public interface Plot extends FigureElement, LabelledComponent {

	List<Series<?>> getSeries();
	Plot withSeries(Series<?> series);
	
	default Set<Scale<?>> getScales() {
		return getSeries().stream().flatMap(s -> s.getScales().stream()).collect(Collectors.toSet());
	};
}
