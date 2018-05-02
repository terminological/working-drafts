package uk.co.terminological.simplechart;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	ChartType type;
	List<X> data;
	List<Tuple<Dimension,Function<X,Object>>> bindings = new ArrayList<>();
	
	public Chart(ChartType type,List<X> data) {
		this.data = data;
		this.type = type;
	}
	
	public static <X> Chart<X> create(ChartType type,List<X> data) {
		return new Chart<>(type,data);
	}
	
	public Chart<X> with(Dimension dimension, Function<X,Object> binding) {
		bindings.add(Tuple.create(dimension, binding));
		return this;
	};
	
	public void render() {
		Writer.write(this);
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL
	}
	
	
	
}
