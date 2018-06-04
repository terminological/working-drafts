package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;
import static uk.co.terminological.simplechart.Chart.Dimension.Y_FIT;

import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;

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

	public static class Fitted extends Kumaraswamy {
		
		double a;
		double b;
		
		public Fitted(double[] params) {
			a = params[0];
			b = params[1];
		}
		
		public double value(double x) {
			return value(x, new double[] {a,b});
		}
		
		public double gradient(double x) {
			return a*b*Math.pow(x, a-1)*Math.pow(1-Math.pow(x, a), b-1);
		}
		
		public String toString() {
			return "a="+Double.toString(a)+"; b="+Double.toString(b);
		}
		
		public void plot(File outfile) throws IOException, TemplateException {
			Figure.Data<Double> figures = Figure.outputTo(outfile)
					.withDefaultData(Figure.Parameter.fromRange(0,1));
			
			figures.withNewChart("gx", ChartType.XY_LINE)
				.bind(X, x -> x)
				.bind(Y, x -> value(x))
				.withAxes("x","kumaraswamy")
				.config().withTitle(this.toString())
				.withXScale(0F, 1F)
				.render();
		}
		
	}
}
