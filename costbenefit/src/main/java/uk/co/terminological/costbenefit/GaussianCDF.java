package uk.co.terminological.costbenefit;

import java.util.function.Function;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;

public class GaussianCDF implements ParametricUnivariateFunction {

	static SimpsonIntegrator integrator = new SimpsonIntegrator(0.001D, 0.001D,2,20);
	
	@Override
	public double value(double x, double... parameters) {
		return integrator.integrate(5, new Gaussian(parameters[0], parameters[1]), Double.NEGATIVE_INFINITY, x);
	}

	@Override
	public double[] gradient(double x, double... parameters) {
		return new double[] {new Gaussian(parameters[0], parameters[1]).value(x)};
	}

	static Function<Double,Double> fn(double mean, double variance) {
		return d -> new GaussianCDF().value(d, mean, variance);
	}
	
}
