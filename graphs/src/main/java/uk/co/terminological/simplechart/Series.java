package uk.co.terminological.simplechart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.Chart.Dimension;

public class Series<X> {

	private List<X> data;
	private List<Triple<Dimension,Function<X,? extends Object>,String>> bindings = new ArrayList<>();
	private Map<Dimension,Comparator<Object>> sorters = new HashMap<>();
	private ColourScheme scheme = ColourScheme.Accent;
	private Chart chart;
	
	// ========= Fluent contructors ==========
	
	public Series(List<X> data, Chart chart) {
		this.data = data;
		this.chart = chart;
	}
	
	public Series<X> bind(Dimension dimension, Function<X,? extends Object> binding) {
		bindings.add(Triple.create(dimension, binding, chart.config.getLabel(dimension)));
		return this;
	};
	
	public Series<X> bind(Dimension dimension, Function<X,? extends Object> binding, String label) {
		bindings.add(Triple.create(dimension, binding, label));
		return this;
	};
	
	public Series<X> withSorting(Dimension dimension, Comparator<Object> sorter) {
		sorters.put(dimension, sorter);
		return this;
	};
	
	public Series<X> withColourScheme(ColourScheme scheme) {
		this.scheme = scheme;
		return this;
	};
	
	public Chart done() {
		return chart;
	};
	
	// ======= Pojo methods ====
	
	
	protected List<X> getData() {
		return data;
	}

	protected List<Triple<Dimension, Function<X, ? extends Object>, String>> getBindings() {
		return bindings;
	}

	protected Map<Dimension, Comparator<Object>> getSorters() {
		return sorters;
	}

	protected Chart getChart() {
		return chart;
	}
	
	public ColourScheme getScheme() {
		return scheme;
	}
	
	@SuppressWarnings("unchecked")
	protected <Y> List<Y> distinctValuesForDimension(Dimension dim) {
		Comparator<Object> sorter = this.getSorters().getOrDefault(dim, 
				(o1,o2) -> o1.toString().compareTo(o2.toString()));
		SortedSet<Object> union = new TreeSet<>(sorter);
		union.addAll(this.valuesForDimensions(dim));
		List<Y> out = new ArrayList<>();
		union.forEach(o -> out.add((Y) o));
		return out;
	}
	
	@SuppressWarnings("unchecked")
	protected <Y> List<Y> valuesForDimensions(Dimension... dims) {
		List<Y> out = new ArrayList<>();
		List<Dimension> dimensions = Arrays.asList(dims);
		bindings.stream()
			.filter(trip -> dimensions.contains(trip.getFirst()))
			.forEach(fn -> {
				getData().stream().map(fn.getSecond()).map(o -> (Y) o).forEach(y -> out.add(y));
			});
		return out;
	}
	
	@SuppressWarnings("unchecked")
	protected <Y> List<Y> valuesForDimension(Dimension dim, String name) {
		return getData().stream().map(functionFor(dim,name)).map(o -> (Y) o).collect(Collectors.toList());
	}
	
	public Function<X,? extends Object> functionFor(Dimension dim, String name) {
		return bindings.stream()
			.filter(trip -> trip.firstEquals(dim) && trip.thirdEquals(name))
			.map(trip -> trip.getSecond())
			.findFirst().get();
	}
	
	public Function<X,? extends Object> functionFor(Dimension dim) {
		return functionFor(dim, "");
	}
	
	// ======= Freemarker accessories ======

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
