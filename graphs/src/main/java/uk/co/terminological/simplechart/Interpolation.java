package uk.co.terminological.simplechart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;

import com.google.common.primitives.Doubles;

public class Interpolation<IN> {

	public static <Y> Interpolation<Y> empty() {
		return new Interpolation<Y>(null,null,null);
	}

	private MultivariateFunction interp;
	private List<Function<IN, Double>> adaptors;
	private double[] density;


	public Interpolation(MultivariateFunction multivariateFunction, List<Function<IN, Double>> adaptors, double[] density) {
		this.interp = multivariateFunction;
		this.adaptors = adaptors;
		this.density = density;
	}

	public Double interpolate(IN input) {
		double[] current = new double[adaptors.size()];
		for (int i=0; i<current.length; i++) {
			current[i] = adaptors.get(i).apply(input);
		}
		return interpolate(current);
	}

	public Double interpolate(List<Double> inputs) {
		return interpolate(Doubles.toArray(inputs));
	}
	
	public Double interpolate(double[] inputs) {
		return interp.value(inputs);
	}
	
	private List<UnivariateFunction> forPosition(final double[] coord) {
		List<UnivariateFunction> out = new ArrayList<>();
		for (int i = 0; i<coord.length; i++) {
			int index = i;
			out.add(new UnivariateFunction() {
				@Override
				public double value(double x) {
					double[] tmp = Arrays.copyOf(coord, coord.length);
					tmp[index]=x;
					return interp.value(tmp);
				}
			});
		}
		return out;
	}

	private List<UnivariateDifferentiableFunction> differentiateAt(final double[] coord) {
		List<UnivariateDifferentiableFunction> out = new ArrayList<>();
		List<UnivariateFunction> uvf = forPosition(coord);
		for (int i = 0; i<coord.length; i++) {
			Double dimensionalDensity = density[i];
			FiniteDifferencesDifferentiator fdd = new FiniteDifferencesDifferentiator(5,dimensionalDensity/10);
			out.add(fdd.differentiate(uvf.get(i)));
		}
		return out;
	}
	
	public List<Double> partialDerivatives(IN input) {
		double[] current = new double[adaptors.size()];
		for (int i=0; i<current.length; i++) {
			current[i] = adaptors.get(i).apply(input);
		}
		return partialDerivatives(current);
	}
	
	public List<Double> partialDerivatives(final double[] coord) {
		List<UnivariateDifferentiableFunction> uvdf = differentiateAt(coord);
		List<Double> out = new ArrayList<>();
		for (int i=0; i<coord.length; i++) {
			DerivativeStructure xDS = new DerivativeStructure(1, 1, 0, coord[i]);
			out.add(uvdf.get(i).value(xDS).getPartialDerivative(1));
		}
		return out;
	}
	
	public <X,Y> Function<X,Y> streamInterpolater(
			Function<X,Y> mapper,
			BiConsumer<Y,Double> valueSetter,
			BiConsumer<Y,List<Double>> derivativeSetter,
			@SuppressWarnings("unchecked") Function<X,Double>... parameterAdaptors
			) {
				return new Function<X,Y>() {
					@Override
					public Y apply(X input) {
						
						double[] current = new double[parameterAdaptors.length];
						for (int i=0; i<current.length; i++) {
							current[i] = parameterAdaptors[i].apply(input);
						}
						
						Y y = mapper.apply(input);
						valueSetter.accept(y, interpolate(current));
						derivativeSetter.accept(y, partialDerivatives(current));
						return y;
					}
					
				};
	}
	
	public <X> Function<X,X> streamAugmenter(
			BiConsumer<X,Double> valueSetter,
			BiConsumer<X,List<Double>> derivativeSetter,
			@SuppressWarnings("unchecked") Function<X,Double>... parameterAdaptors) {


			return new Function<X,X>() {
				@Override
				public X apply(X input) {
					
					double[] current = new double[parameterAdaptors.length];
					for (int i=0; i<current.length; i++) {
						current[i] = parameterAdaptors[i].apply(input);
					}
					
					valueSetter.accept(input, interpolate(current));
					derivativeSetter.accept(input, partialDerivatives(current));
					return input;
				}
				
		};
	}

}