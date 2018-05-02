package uk.co.terminological.simplechart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import uk.co.terminological.datatypes.Tuple;

public class Chart<X> {

	List<X> data;
	List<Tuple<Dimension,Function<X,Object>>> bindings = new ArrayList<>();
	
	public Chart(List<X> data) {
		this.data = data;
	}
	
	public static <X> Chart<X> create(List<X> data) {
		return new Chart<>(data);
	}
	
	public Chart<X> with(Dimension dimension, Function<X,Object> binding) {
		bindings.add(Tuple.create(dimension, binding));
		return this;
	};
	
	public void render(ChartType type) {
		Writer.write(this);
	}
	
	public void render(String templateName) {
		Writer.write(this);
	}
	
	public enum Dimension {
		X,Y,Z,COLOUR,SIZE,LABEL
	}
	
	
	
}
