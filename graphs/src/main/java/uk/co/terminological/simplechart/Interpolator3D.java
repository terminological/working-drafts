package uk.co.terminological.simplechart;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.EavMap;

public class Interpolator3D<IN> implements Collector<IN, EavMap<Double,Double,Double>, Result> {

	Function<IN,Double> xAdaptor;
	Function<IN,Double> yAdaptor;
	Function<IN,Double> zAdaptor;
	
	
	public Interpolator3D(Function<IN,Double> xAdaptor, Function<IN,Double> yAdaptor, Function<IN,Double> zAdaptor ) {
		this.xAdaptor = xAdaptor;
		this.yAdaptor = yAdaptor;
		this.zAdaptor = zAdaptor;
	}
	
	public static <X> Result fromStream(Stream<X> input, Function<X,Double> xAdaptor, Function<X,Double> yAdaptor, Function<X,Double> zAdaptor) {
		return input.collect(new Interpolator3D(xAdaptor,yAdaptor,zAdaptor));
	}
	
	
	public static class Result {
		
	}


	@Override
	public Supplier<EavMap<Double, Double, Double>> supplier() {
		return () -> new EavMap<Double,Double,Double>();
	}


	@Override
	public BiConsumer<EavMap<Double, Double, Double>, IN> accumulator() {
		return (map, input) -> {
			map.add(xAdaptor.apply(input), yAdaptor.apply(input), zAdaptor.apply(input));
		};
	}


	@Override
	public BinaryOperator<EavMap<Double, Double, Double>> combiner() {
		return (map1,map2) -> map1.addAll(map2);
	}


	@Override
	public Function<EavMap<Double, Double, Double>, Result> finisher() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<Characteristics> characteristics() {
		// TODO Auto-generated method stub
		return null;
	}
}
