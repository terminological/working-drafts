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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.interpolation.BicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.BivariateGridInterpolator;
import org.apache.commons.math3.analysis.interpolation.MicrosphereProjectionInterpolator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolator;
import org.apache.commons.math3.util.MathArrays;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.datatypes.FluentSet;

public class Interpolator<IN> implements Collector<IN, List<List<Double>>, Interpolation<IN>> {

	List<Function<IN,Double>> inputAdaptors;
	Function<IN,Double> valueAdaptor;
	
	public Interpolator(Function<IN,Double> valueAdaptor, @SuppressWarnings("unchecked") Function<IN,Double>... inputAdaptor) {
		this.inputAdaptors = Arrays.asList(inputAdaptor);
		this.valueAdaptor = valueAdaptor;
	}
	
	public static <X> Interpolation<X> fromStream(Stream<X> input, Function<X,Double> valueAdaptor, @SuppressWarnings("unchecked") Function<X,Double>... inputAdaptor) {
		return input.collect(new Interpolator<X>(valueAdaptor,inputAdaptor));
	}
	
	/*
dimension - Space dimension.
elements - Number of surface elements of the microsphere.
exponent - Exponent used in the power law that computes the
maxDarkFraction - Maximum fraction of the facets that can be dark. If the fraction of "non-illuminated" facets is larger, no estimation of the value will be performed, and the background value will be returned instead.
darkThreshold - Value of the illumination below which a facet is considered dark.
background - Value returned when the maxDarkFraction threshold is exceeded.
sharedSphere - Whether the sphere can be shared among the interpolating function instances. If true, the instances will share the same data, and thus will not be thread-safe.
noInterpolationTolerance - When the distance between an interpolated point and one of the sample points is less than this value, no interpolation will be performed (the value of the sample will be returned).
	 */
	
    int elementsPerDimension = 4;
    double maxDarkFraction = 1.0;
    double darkThreshold = 0.0;
    double exponent = 2;
    boolean sharedSphere = false;
    double noInterpolationTolerance = 0D;
	
	
	@Override
	public Supplier<List<List<Double>>> supplier() {
		return () -> new ArrayList<>();
	}


	@Override
	public BiConsumer<List<List<Double>>, IN> accumulator() {
		return (map, input) -> {
			List<Double> current = new ArrayList<>();
			current.add(valueAdaptor.apply(input));
			inputAdaptors.forEach(fn -> current.add(fn.apply(input)));
			map.add(current);
		};
	}


	@Override
	public BinaryOperator<List<List<Double>>> combiner() {
		return (map1,map2) -> {
			map1.addAll(map2);
			return map1;
		};
	}


	@Override
	public Function<List<List<Double>>, Interpolation<IN>> finisher() {
		return (map) -> {
			if (map.size() < 1) return Interpolation.empty();
			int coordinates = map.size();
			double[] yval = new double[coordinates];
			int dimensions = inputAdaptors.size();
			double[][] xvals = new double[dimensions][coordinates];
			double sum = 0D;
			double max[] = new double[dimensions];
			Arrays.fill(max, Double.MIN_VALUE);
			double min[] = new double[dimensions];
			Arrays.fill(min, Double.MAX_VALUE);
			double count[] = new double[dimensions];
			Arrays.fill(count, coordinates);
			
			for (int i = 0; i<coordinates; i++) {
				yval[i] = map.get(i).get(0);
				sum += yval[i]; 
				
				for (int j=0; j<dimensions; j++) {
					double tmp = map.get(i).get(j+1);
					xvals[j][i] = tmp;
					if (min[j] > tmp) min[j] = tmp; 
					if (max[j] < tmp) max[j] = tmp;
				}
			}
			
			double[] diff = MathArrays.ebeSubtract(max,min);
			double[] density = MathArrays.ebeDivide(diff,count);
			
			Interpolation<IN> out = new Interpolation<IN>(
					new MicrosphereProjectionInterpolator(dimensions,
	                    (int) Math.pow(elementsPerDimension,dimensions),
	                    maxDarkFraction,
	                    darkThreshold,
	                    sum/coordinates,
	                    exponent,
	                    sharedSphere,
	                    noInterpolationTolerance).interpolate(xvals, yval), 
					inputAdaptors,
					Arrays.stream(density).boxed().collect(Collectors.toList())
					);
			return out;
		};
	}


	@Override
	public Set<Characteristics> characteristics() {
		return FluentSet.create(Characteristics.UNORDERED, Characteristics.CONCURRENT);
	}
}
