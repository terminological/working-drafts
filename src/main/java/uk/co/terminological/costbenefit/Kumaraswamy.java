package uk.co.terminological.costbenefit;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;

/**
 * Implementation of a parametric Kumaraswamy distribution where we fit the cumulative distribution function
 * and the first derivative is the probability distribution function. This will generally fit signoidal probability
 * curves between 0 and 1 
 * https://en.wikipedia.org/wiki/Kumaraswamy_distribution
 * @author rc538
 *
 */
public class Kumaraswamy implements ParametricUnivariateFunction {

	/**
	 * dx/da = b*x^a*(1-x^a)^(b-1)*log(x)
	 * 
	 * dx/db = -(1-x^a)^b*log(1-x^a)
	 */
	@Override
	public double[] gradient(double x, double... parameters) throws NullArgumentException, DimensionMismatchException {
		if (parameters.length != 2) throw new DimensionMismatchException(parameters.length, 2);
		if (x < 0 || x > 1) throw new NullArgumentException();
		double a = parameters[0];
		double b = parameters[1];
		double xa = Math.pow(x, a); 
		return new double[] {
				b*xa*Math.pow(1-xa,b-1)*Math.log(x)
				,
				-Math.pow(1-xa,b)*Math.log(1-xa)
		};
		
	}

	/**
	 * y = 1-(1-x^a)^b
	 */
	@Override
	public double value(double x, double... parameters) throws NullArgumentException, DimensionMismatchException {
		if (parameters.length != 2) throw new DimensionMismatchException(parameters.length, 2);
		if (x < 0 || x > 1) throw new NullArgumentException();
		double a = parameters[0];
		double b = parameters[1];
		return 1-Math.pow(
				(1-Math.pow(x, a))
				,b);
	}

}
