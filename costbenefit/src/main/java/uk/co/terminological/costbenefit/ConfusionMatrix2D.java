package uk.co.terminological.costbenefit;

import org.apache.commons.math3.util.Precision;

import uk.co.terminological.costbenefit.ClassifierModel.CostModel;

public class ConfusionMatrix2D {

	double tp;
	double tn;
	double fp;
	double fn;
	
	int total;
	
	double tpValue; 
	double tnValue; 
	double fpCost; 
	double fnCost;
	double prevalence;
	
	public ConfusionMatrix2D(int TP, int TN, int FP, int FN) {
		total = TP+TN+FP+FN;
		tp = TP/((double) total);
		fp = FP/((double) total);
		tn = TN/((double) total);
		fn = FN/((double) total);
	}
	
	public ConfusionMatrix2D(double TPR, double TNR, double FPR, double FNR) {
		total = 1;
		if (!Precision.equals(TPR+TNR+FPR+FNR,1.0,3))  
			throw new ConstraintViolationException("Sum of paramters must be 1");
		tp = TPR;
		fp = FPR;
		tn = TNR;
		fn = FNR;
	}
	
	public ConfusionMatrix2D withCostModel(CostModel model, double prevalence) {
		return withCostModel(model.tpValue(),model.tnValue(),model.fpCost(),model.fnCost(),prevalence);
	}
	
	public ConfusionMatrix2D withCostModel(double tpValue, double tnValue, double fpCost, double fnCost, double prevalence) {
		this.tpValue = tpValue;
		this.tnValue = tnValue;
		this.fpCost = fpCost;
		this.fnCost= fnCost;
		this.prevalence= prevalence;
		return this;
	}
	
	public double sensitivity() {return truePositiveRate();}
	public double specificity() {return trueNegativeRate();}
	
	
	public double recall() {return truePositiveRate();}
	public double precision() {return positivePredictiveValue();}
	
	
	public double truePositiveRate() {return tp/(tp+fn);}
	public double trueNegativeRate() {return tn/(tn+fp);}
	public double falseNegativeRate() {return fn/(fn+tp);}
	public double falsePositiveRate() {return fp/(fp+tn);}
	
	public double positivePredictiveValue() {return tp/(tp+fp);}
	public double negativePredictiveValue() {return tn/(tn+fn);}
	public double falseDiscoveryRate() {return fp/(fp+tp);}
	public double falseOmissionRate() {return fn/(fn+tn);}
	
	public double accuracy() {return (tp+tn)/total;}
	public double matthewsCorrelationCoefficient() {return (tp*tn-fp*fn)/Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));}
	public double fScore(double beta) {
		double b2 = Math.pow(beta, 2);
		return ((1+b2)*tp)/((1+b2)*tp+b2*fn+fp);
	}
	public double f1Score() {return fScore(1D);}
	public double youdensJ() {return truePositiveRate()+trueNegativeRate()-1;}
	
	public double absoluteValue() {
		return absoluteValue(tpValue, tnValue, fpCost, fnCost);
	}
	
	public double relativeValue() {
		return relativeValue(tpValue, tnValue, fpCost, fnCost, prevalence);
	}
	
	public double absoluteValue(double tpValue, double tnValue, double fpCost, double fnCost) {
		if (tpValue < 0 || tnValue < 0) throw new ConstraintViolationException("Values of true positives and negatives must be larger than zero");
		if (fpCost > 0 || fnCost > 0) throw new ConstraintViolationException("Costs of false positives and negatives must be smaller than zero");
		return tpValue*tp+tnValue*tn+fpCost*fp+fnCost*fn;
	}
	
	public double relativeValue(double tpValue, double tnValue, double fpCost, double fnCost, double prevalence) {
		double maxValue = tpValue*prevalence + tnValue*(1-prevalence);
		double minCost = fnCost*prevalence + fpCost*(1-prevalence);
		return (absoluteValue(tpValue,tnValue,fpCost,fnCost)-minCost)/(maxValue-minCost);
	}
	
	private double pmi(double pxy, double px, double py) {
		if (pxy < 0D || pxy > 1D || px < 0D || px > 1D || py < 0D || py > 1D || pxy > px || pxy > py ) throw new RuntimeException("Parameters invalid");
		if (px==0 || py==0) return 0D; //pxy must also be zero at this point. could return NaN...
		if (pxy == 0) return Double.NEGATIVE_INFINITY;
		return Math.log(pxy/(px*py));
	}
	
	public double mi() {
		double px1y1 = tp;
		double px0y1 = fn;
		double px1y0 = fp;
		double px0y0 = tn;
	
		if (px1y1 < 0D || px1y1 > 1D || px0y1 < 0D || px0y1 > 1D || px1y0 < 0D || px1y0 > 1D ) throw new RuntimeException("Parameters invalid");
		
		double px1 = px1y0+px1y1;
		double px0 = 1.0-px1;
		double py1 = px1y1+px0y1;
		double py0 = 1.0-py1;
		return px1y1*pmi(px1y1,px1,py1)
				+px0y1*pmi(px0y1,px0,py1)
				+px1y0*pmi(px1y0,px1,py0)
				+px0y0*pmi(px0y0,px0,py0);
	}
}
