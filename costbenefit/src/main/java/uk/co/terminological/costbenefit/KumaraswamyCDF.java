package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.SeriesBuilder;

/**
 * Implementation of a parametric Kumaraswamy distribution where we fit the cumulative distribution function
 * and the first derivative is the probability distribution function. This will generally fit signoidal probability
 * curves between 0 and 1 
 * https://en.wikipedia.org/wiki/Kumaraswamy_distribution
 * @author rc538
 *
 */
public class KumaraswamyCDF implements ParametricUnivariateFunction {

	
	public static Function<Double,Double> cdf(Double a, Double b) {
		return x -> new KumaraswamyCDF().value(x,a,b);
	}
	
	public static Function<Double,Double> invCdf(Double a, Double b) {
		return y -> Math.pow((1-Math.pow((1-y), 1/b)), 1/a);
	}
	
	public static Function<Double,Double> pdf(Double a, Double b) {
		return x -> a*b*Math.pow(x, a-1)*Math.pow(1-Math.pow(x, a), b-1);
	}
	
	/**
	 * dx/da = b*x^a*(1-x^a)^(b-1)*log(x)
	 * 
	 * dx/db = -(1-x^a)^b*log(1-x^a)
	 */
	@Override
	public double[] gradient(double x, double... parameters) throws NullArgumentException, DimensionMismatchException {
		if (parameters.length != 2) throw new DimensionMismatchException(parameters.length, 2);
		if (x < 0 || x > 1) throw new NullArgumentException();
		if (x==0 || x==1) return new double[] {0,0};
		double a = parameters[0];
		double b = parameters[1];
		double xa = Math.pow(x, a); 
		return new double[] {
				b*xa*Math.pow(1-xa,b-1)*Math.log(x)	,
				-Math.pow(1-xa,b)*Math.log(1-xa)
		};
		
	}
	
	/*public static UnivariateFunction aParameter(Double mode, Double iqr) {
		return a -> 
			Math.pow((1-Math.pow(0.25,(a*Math.pow(mode,a)/(Math.pow(mode,a)+a-1)))),1/a)-
			Math.pow((1-Math.pow(0.75,(a*Math.pow(mode,a)/(Math.pow(mode,a)+a-1)))),1/a)-
			iqr;
	 }

	
	public static Double a(Double iqr, Double mode) {
		return new BrentSolver().solve(500, aParameter(mode,iqr), 1,10000,10);
	}*/
	
	public static Double a(Double iqr, Double mode) {
		return (mode+iqr)/mode;
	}
	
	public static Double b(Double spread, Double mode) {
		Double a = a(spread,mode);
		return (-1+a+Math.pow(mode, a))/(a*Math.pow(mode, a));
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

	public static class Fitted extends KumaraswamyCDF {
		
		double a;
		double b;
		boolean invert;
		
		public Fitted(double[] params, boolean c) {
			a = params[0];
			b = params[1];
			invert = c;
		}
		
		public double value(double x) {
			return invert ? 
					1-value(x, new double[] {a,b}) :
					value(x, new double[] {a,b});
		}
		
		public double gradient(double x) {
			return (invert ? -1 : 1)*
					a*b*Math.pow(x, a-1)*Math.pow(1-Math.pow(x, a), b-1);
		}
		
		public String toString() {
			return "a="+Double.toString(a)+"; b="+Double.toString(b);
		}
		
		public void plot(File outfile, String name) throws IOException, TemplateException {
			Figure figures = Figure.outputTo(outfile);
					//.withDefaultData(Figure.Parameter.fromRange(0,1));
			
			figures.withNewChart(name, ChartType.XY_LINE)
				.config()
					.withXLabel("x")
					.withYLabel("cumulative")
					.withTitle(this.toString())
					.withXScale(0F, 1F)
				.done()
				.withSeries(SeriesBuilder.range(0D, 1D, 1000))
					.bind(X, x -> x)
					.bind(Y, x -> value(x))
				.done()
				.render();
		}
		
	}
}
