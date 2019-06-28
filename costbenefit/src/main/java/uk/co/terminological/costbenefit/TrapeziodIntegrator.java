package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import uk.co.terminological.datatypes.FluentSet;
import uk.co.terminological.datatypes.Tuple;

public class TrapeziodIntegrator {

	public static Double integrate(List<Tuple<Double,Double>> input) {
		//Sort by first values
		input.sort((t1,t2) -> t1.getFirst().compareTo(t2.getFirst()));
		Double sum = 0D;
		for (int i = 1; i<input.size(); i++) {
			sum += trapeziod(input.get(i-1).getFirst(), input.get(i).getFirst(), input.get(i-1).getSecond(), input.get(i).getSecond());
		}
		return sum;
	}

	private static Double trapeziod(Double x1, Double x2, Double y1, Double y2) {
		return (y1+y2)/2*(x2-x1);
	}
	
	public static Collector<Tuple<Double,Double>,List<Tuple<Double,Double>>,Double> integrator() {
		return new Collector<Tuple<Double,Double>,List<Tuple<Double,Double>>,Double>() {

			@Override
			public Supplier<List<Tuple<Double, Double>>> supplier() {
				return () -> new ArrayList<Tuple<Double,Double>>();
			}

			@Override
			public BiConsumer<List<Tuple<Double, Double>>, Tuple<Double, Double>> accumulator() {
				return (l,t) -> l.add(t);
			}

			@Override
			public BinaryOperator<List<Tuple<Double, Double>>> combiner() {
				return (l1,l2) -> {
					ArrayList<Tuple<Double, Double>> tmp = new ArrayList<Tuple<Double, Double>>(l1);
					tmp.addAll(l2);
					return tmp;
				};
			}

			@Override
			public Function<List<Tuple<Double, Double>>, Double> finisher() {
				return in -> integrate(in);
			}

			@Override
			public Set<Characteristics> characteristics() {
				return FluentSet.create(Characteristics.CONCURRENT, Characteristics.UNORDERED);
			}
			
		};
	}
}
