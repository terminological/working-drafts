package uk.co.terminological.costbenefit;

import java.util.function.Function;

public class SimpsonIntegrator {

	long N = 10000;                    // precision parameter
	
	public SimpsonIntegrator(int decimalPlaces) {
		N = (long) Math.pow(10, decimalPlaces); 
	}
	
	public double integrate(double a, double b, Function<Double,Double> f) {
		
		double h = (b - a) / (N - 1);     // step size

		// 1/3 terms
		double sum = 1.0 / 3.0 * (f.apply(a) + f.apply(b));

		// 4/3 terms
		for (int i = 1; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 4.0 / 3.0 * f.apply(x);
		}

		// 2/3 terms
		for (int i = 2; i < N - 1; i += 2) {
			double x = a + h * i;
			sum += 2.0 / 3.0 * f.apply(x);
		}

		return sum * h;
	}

}
