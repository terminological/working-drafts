package uk.co.terminological.simplechart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolator;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.datatypes.FluentSet;

public class Interpolator<IN> implements Collector<IN, EavMap<Double,Double,Double>, Result> {

	Function<IN,Double> xAdaptor;
	Function<IN,Double> yAdaptor;
	Function<IN,Double> zAdaptor;
	
	int smoothing = 0;
	
	public Interpolator(Function<IN,Double> xAdaptor, Function<IN,Double> yAdaptor, Function<IN,Double> zAdaptor ) {
		this.xAdaptor = xAdaptor;
		this.yAdaptor = yAdaptor;
		this.zAdaptor = zAdaptor;
	}
	
	public static <X> Result fromStream(Stream<X> input, Function<X,Double> xAdaptor, Function<X,Double> yAdaptor, Function<X,Double> zAdaptor) {
		return input.collect(new Interpolator(xAdaptor,yAdaptor,zAdaptor));
	}
	
	public static <X> Result fromStream(Stream<X> input, Function<X,Double> xAdaptor, Function<X,Double> yAdaptor, Function<X,Double> zAdaptor, int smoothing) {
		return input.collect(new Interpolator(xAdaptor,yAdaptor,zAdaptor).withSmoothing(smoothing));
	}
	
	
	public Interpolator<IN> withSmoothing(int smoothing) {
		this.smoothing = smoothing;
		return this;
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


	@SuppressWarnings("deprecation")
	@Override
	public Function<EavMap<Double, Double, Double>, Result> finisher() {
		return (map) -> {
			
			List<Double> xValues = new ArrayList<Double>(map.getEntitySet());
			Collections.sort(xValues);
			List<Double> yValues = new ArrayList<Double>(map.getAttributeSet());
			Collections.sort(yValues);
			double[][] matrix = new double[xValues.size()][yValues.size()];
			for (int i = 0; i<xValues.size(); i++) {
				for (int j = 0; j<yValues.size(); j++) {
					matrix[i][j] = map.getOrElse(xValues.get(i), yValues.get(j), Double.NaN);
				}
			}
			
			BivariateGridInterpolator interp;
			if (smoothing == 0) {
				interp = new PiecewiseBicubicSplineInterpolator();
			} else {
				interp = new SmoothingPolynomialBicubicSplineInterpolator(smoothing);
			}
			
			
		};
	}


	@Override
	public Set<Characteristics> characteristics() {
		return FluentSet.create(Characteristics.UNORDERED, Characteristics.CONCURRENT);
	}
}
