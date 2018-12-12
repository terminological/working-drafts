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
	
	// ======= Freemarker accessories ======
	
	public Function<X,Object> functionFor(Dimension dim, String name) {
		return bindings.stream()
			.filter(trip -> trip.firstEquals(dim) && trip.thirdEquals(name))
			.map(trip -> trip.getSecond())
			.findFirst().get();
	}
	
	public Function<X,Object> functionFor(Dimension dim) {
		return bindings.stream()
			.filter(trip -> trip.firstEquals(dim))
			.map(trip -> trip.getSecond())
			.findFirst().get();
	}
	
	public List<Integer> indexesOf(String dim) {
		List<Integer> out = new ArrayList<>();
		int i=1;
		for (Triple<Dimension, ?, String> binding: bindings) {
			if (binding.getFirst().equals(Chart.Dimension.valueOf(dim))) out.add(i);
			i++;
		}
		return out;
	}

	public int indexOf(String dim) {
		int i=1;
		for (Triple<Dimension, ?, String> binding: bindings) {
			if (binding.getFirst().equals(Chart.Dimension.valueOf(dim))) return i;
			i++;
		}
		return -1;
	}

	public boolean hasDimension(String dim) {
		return indexOf(dim) != -1;
	}

	//i is one based as based on GNUplot
	public String getLabelFor(int i) {
		return bindings.get(i-1).getThird();
	}
}
