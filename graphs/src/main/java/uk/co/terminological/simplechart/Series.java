package uk.co.terminological.simplechart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Series<X> {

	List<X> data;
	List<Triple<Dimension,Function<X,Object>,String>> bindings = new ArrayList<>();
	Chart chart;
	
	public Series(List<X> data, Chart chart) {
		this.data = data;
		this.chart = chart;
	}
	
	public Series<X> bind(Dimension dimension, Function<X,Object> binding) {
		bindings.add(Triple.create(dimension, binding, ""));
		return this;
	};
	
	public Series<X> bind(Dimension dimension, Function<X,Object> binding, String label) {
		bindings.add(Triple.create(dimension, binding, label));
		return this;
	};
	
	public Chart done() {
		return chart;
	};
}
