package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y_LINE;

import java.util.function.Function;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.util.Precision;

import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.Chart;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.simplechart.SeriesBuilder;

public class Kumaraswamy extends ClassifierModel<Double> {
	
	Double aP;
	Double bP;
	Double aQ;
	Double bQ;
	
	Function<Double,Double> pFunc;
	Function<Double,Double> qFunc;
	Function<Double,Double> PFunc;
	Function<Double,Double> QFunc;
	
	String name;
	
	static Double modePFromDiv(Double div, Double skew) {return skew/4D+div/4D+1D/2D;}
	static Double modeQFromDiv(Double div, Double skew) {return skew/4D-div/4D+1D/2D;}
	static Double iqrPFromDiv(Double div, Double skew) {return 1D/2D-div/2D;}
	static Double iqrQFromDiv(Double div, Double skew) {return 1D/2D-div/2D;}
	
	static Double bP(Double aP,Double modeP) {return (1+Math.pow((-Math.pow((aP-1),(1/aP))/(modeP-1)),aP))/aP;}
	static Double bQ(Double aQ,Double modeQ) {return (1+Math.pow((Math.pow((aQ-1),(1/aQ))/(modeQ)),aQ))/aQ;}
	
	private static Double iqr(Double a,Double b) {return
		-(
			Math.pow(
				(Math.pow(2,(2/b))-Math.pow(3,(1/b))),(1/a)
			)-Math.pow(
				(Math.pow(2,(1/b))-1),(1/a)
			)*Math.pow(
					(1+Math.pow(2,(1/b))),(1/a)
				)
		)/Math.pow(2,(2/(a*b)));
	}
	
	public static Double aP(Double iqrP, Double modeP) {
		try {
			return new BrentSolver().solve(500, 
					aP -> iqrP(aP,modeP)-iqrP, 1,10000,10);
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
	public static Double aQ(Double iqrQ, Double modeQ) {
		try {
			return new BrentSolver().solve(500, 
					aQ -> iqrQ(aQ,modeQ)-iqrQ, 1,10000,10);
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	
	static Double iqrP(Double aP, Double modeP) {return iqr(aP,bP(aP,modeP));}
	static Double iqrQ(Double aQ, Double modeQ) {return iqr(aQ,bQ(aQ,modeQ));}
	
	static Double P(Double x, Double aP, Double bP) {
		return Math.pow(1-(Math.pow((1-x), aP)),bP);	}
	
	static Double p(Double x, Double aP, Double bP) {
		return bP*P(x,aP,bP-1)*
				aP*Math.pow((1-x),(aP-1));
	}
	
	
	static Double Q(Double x, Double aQ, Double bQ) {
		return 1-Math.pow((1-Math.pow(x, aQ)),bQ);	}
	
	static Double q(Double x, Double aQ, Double bQ) {
		return bQ*Math.pow(1-Math.pow(x,aQ), bQ-1)*
				aQ*Math.pow(x,(aQ-1));
	}
	
	//static Double modeQ2(Double aP,Double bP) {return Math.pow((aP-1),(1/aP))/Math.pow((aP*bP-1),(1/aP));}
	
	
	
	public Kumaraswamy(ClassifierConfig config) {
		this(config.divergence(), config.skew(), config.toString());
	}
	
	/**
	 * 
	 * @param divergence between 0 and 1.
	 * @param name
	 */
	public Kumaraswamy(Double divergence, String name) {
		this(divergence,0D,name); 
		
	}
	
	public Kumaraswamy(Double divergence, Double skew) {
		this(divergence,skew,"");
	}
	
	
	/**
	 * 
	 * @param divergence between 0 and 1.
	 * @param skew between -1 and 1.
	 * @param name
	 */
	public Kumaraswamy(Double divergence, Double skew, String name) {
		/*if (!(divergence > 0 && divergence < 1 &&
				skew > -1 && skew < 1))
			throw new ConstraintViolationException("Divergence must be between 0 and 1, skew must be between -1 and 1");*/
		this(
			modePFromDiv(divergence, skew),
			iqrPFromDiv(divergence, skew),
			modeQFromDiv(divergence, skew),
			iqrQFromDiv(divergence, skew),
			name
		); 
		
	}
	
	
	
	public Kumaraswamy(Double modeP, Double iqrP, Double modeQ, Double iqrQ, String name) {
		
		if (!(modeP > 0 && modeP < 1 &&
				modeQ > 0 && modeQ < 1 &&
				iqrP > 0 && iqrP < 0.5 &&
				iqrQ > 0 && iqrQ < 0.5 &&  
				modeP >= modeQ)) 
			throw new ConstraintViolationException(
					"Modes must be between 0 and 1, IQR must be greater than zero, modePos must be larger than modeNeg :"+
							"modes: ["+modeP+","+modeQ+"], iqrs: ["+iqrP+","+iqrQ+"]" 
					);
		
		
		aP = aP(iqrP, modeP);
		bP = bP(aP, modeP);
		pFunc = x -> p(x,aP,bP);
		PFunc = x -> P(x,aP,bP);
		
		aQ = aQ(iqrQ, modeQ);
		bQ = bQ(aQ, modeQ);
		qFunc = x -> q(x,aQ,bQ);
		QFunc = x -> Q(x,aQ,bQ);
		
		/*aNeg = KumaraswamyCDF.a(spreadNeg, modeNeg);
		bNeg = KumaraswamyCDF.b(spreadNeg, modeNeg);
		pdfGivenNegative = KumaraswamyCDF.pdf(aNeg, bNeg);
		cdfGivenNegative = KumaraswamyCDF.cdf(aNeg, bNeg);*/
		
		this.name = name;
		
	}
	
	public Chart plotPdf(Figure fig) {
		return fig.withNewChart(name+" pdf", ChartType.XY_MULTI_LINE)
		.config().withXScale(0F, 1F)
		.withXLabel("x")
		.withYLabel("density")
		.withYScale(0F, 4F)
		.done()
		.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
		.bind(X, t -> t)
		.bind(Y_LINE, pFunc,"pos pdf")
		.bind(Y_LINE, qFunc,"neg pdf")
		.done();
	}
	
	public Chart plotCdf(Figure fig) {
		return fig.withNewChart(name+" cdf", ChartType.XY_MULTI_LINE)
		.config().withXScale(0F, 1F)
		.withXLabel("x")
		.withYLabel("cumulative")
		.withYScale(0F, 1F)
		.done()
		.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
		.bind(X, t -> t)
		.bind(Y_LINE, PFunc,"pos cdf")
		.bind(Y_LINE, QFunc,"neg cdf")
		//.bind(Y, t -> prev*cdfGivenPositive.apply(t)+prev*cdfGivenNegative.apply(t),"joint cdf")
		.done();
	}
	
	public Chart plotRoc(Figure fig) {
		return fig.withNewChart(name+" roc", ChartType.XY_MULTI_LINE)
		.config().withXScale(0F, 1F)
		.withXLabel("1-sens")
		.withYLabel("spec")
		.withYScale(0F, 1F)
		.done()
		.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
		.bind(X, t -> 1-matrix(0.5,t).sensitivity())
		.bind(Y_LINE, t -> matrix(0.5,t).specificity(),"roc")
		.bind(Y_LINE, t -> 1-matrix(0.5,t).sensitivity(),"identity")
		.done();
	}
	
	public Chart plotPR(Figure fig) {
		return fig.withNewChart(name+" pr", ChartType.XY_MULTI_LINE)
		.config().withXScale(0F, 1F)
		.withYLabel("precision")
		.withXLabel("recall")
		.withYScale(0F, 1F)
		.done()
		.withSeries(SeriesBuilder.range(0D, 1D, 1000)).withColourScheme(ColourScheme.Dark2)
		.bind(X, t -> matrix(0.01,t).recall(), "pr: 0.01")
		.bind(X, t -> matrix(0.05,t).recall(), "pr: 0.05")
		.bind(X, t -> matrix(0.1,t).recall(), "pr: 0.1")
		.bind(X, t -> matrix(0.3,t).recall(), "pr: 0.3")
		.bind(X, t -> matrix(0.5,t).recall(), "pr: 0.5")
		.bind(X, t -> matrix(0.7,t).recall(), "pr: 0.7")
		.bind(X, t -> matrix(0.9,t).recall(), "pr: 0.9")
		.bind(Y_LINE, t -> matrix(0.01,t).precision(), "pr: 0.01")
		.bind(Y_LINE, t -> matrix(0.05,t).precision(), "pr: 0.05")
		.bind(Y_LINE, t -> matrix(0.1,t).precision(), "pr: 0.1")
		.bind(Y_LINE, t -> matrix(0.3,t).precision(), "pr: 0.3")
		.bind(Y_LINE, t -> matrix(0.5,t).precision(), "pr: 0.5")
		.bind(Y_LINE, t -> matrix(0.7,t).precision(), "pr: 0.7")
		.bind(Y_LINE, t -> matrix(0.9,t).precision(), "pr: 0.9")
		.done();
	}
	
	public void plot(Figure fig) {
		fig.withTitle(name);
		plotPdf(fig);
		plotCdf(fig);
		plotRoc(fig);
		fig.render(3, true, false);
	}
	
	public Tuple<Double,Double> bestCutoff(Double prev,Function<ConfusionMatrix2D,Double> feature) {
		Double value = Double.MIN_VALUE;
		Double bestCutoff = Double.NaN;
		for (Double d=0D; d<=1.0D;d += 0.001) {
			ConfusionMatrix2D tmp = matrix(prev,d);
			if (feature.apply(tmp) > value) {
				value = feature.apply(tmp);
				bestCutoff = d;
			}
		}
		return Tuple.create(bestCutoff,value);
	}
	
	public double screeningBeneficial(CostModel model, Double prev) {
		Tuple<Double,Double> tmp = bestCutoff(prev, mat -> mat.normalisedValue(model));
		if (Precision.equals(tmp.getFirst(), 0.0D) || Precision.equals(tmp.getFirst(), 1.0D)) return Double.NaN;
		return tmp.getValue();
	}
	
	public ConfusionMatrix2D matrix(Double prev,Double cutoff) {
		
		Double cdfPos = PFunc.apply(cutoff);
		Double cdfNeg = QFunc.apply(cutoff);
		
		Double eTp = prev*(1-cdfPos);
		Double eTn = (1-prev)*cdfNeg;
		Double eFn = prev*cdfPos;
		Double eFp = (1-prev)*(1-cdfNeg);
		
		//TODO: prevalence is independent of cutoff - this should be correct. Classifier model accounts for prevalence by.
		
		return new ConfusionMatrix2D(eTp,eTn,eFp,eFn);
	}
	
	public Double AUROC() {
		return 
		SeriesBuilder.range(0.0, 1.0, 1000)
			.map(c -> matrix(0.5,c))
			.map(m -> Tuple.create(m.sensitivity(), m.specificity()))
			.collect(TrapeziodIntegrator.integrator());
	}
	
	public Double KLDivergence() {
		Function<Double,Double> p = pFunc;
		Function<Double,Double> q = qFunc;
		Double dpq = 
				SeriesBuilder.range(0.0, 1.0, 1000)
					.map(x -> Tuple.create(x,
							Precision.equals(p.apply(x),0D) || Precision.equals(q.apply(x),0D) 
								? 0 : p.apply(x)*Math.log(p.apply(x)/q.apply(x))))
					.collect(TrapeziodIntegrator.integrator());
		Double dqp = 
				SeriesBuilder.range(0.0, 1.0, 1000)
					.map(x -> Tuple.create(x,
							Precision.equals(p.apply(x),0D) || Precision.equals(q.apply(x),0D) 
								? 0 : q.apply(x)*Math.log(q.apply(x)/p.apply(x))))
					.collect(TrapeziodIntegrator.integrator());
		return dpq+dqp;		
	}
	
	
	public Double LambdaDivergence(Double prev) {
		Function<Double,Double> p = pFunc;
		Function<Double,Double> q = qFunc;
		Function<Double,Double> j = x -> prev*pFunc.apply(x) + (1-prev)*qFunc.apply(x);
		Double dpq = 
				SeriesBuilder.range(0.0, 1.0, 1000)
					.map(x -> Tuple.create(x,
							Precision.equals(p.apply(x),0D) || Precision.equals(j.apply(x),0D) 
								? 0 : p.apply(x)*Math.log(p.apply(x)/j.apply(x))))
					.collect(TrapeziodIntegrator.integrator());
		Double dqp = 
				SeriesBuilder.range(0.0, 1.0, 1000)
					.map(x -> Tuple.create(x,
							Precision.equals(p.apply(x),0D) || Precision.equals(j.apply(x),0D) 
								? 0 : q.apply(x)*Math.log(q.apply(x)/j.apply(x))))
					.collect(TrapeziodIntegrator.integrator());
		return prev*dpq+(1-prev)*dqp;		
	}

	
}