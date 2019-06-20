package uk.co.terminological.costbenefit;

public class ConfusionMatrix {

	Double tp;
	Double tn;
	Double fp;
	Double fn;
	
	int total;
	
	public ConfusionMatrix(int TP, int TN, int FP, int FN) {
		total = TP+TN+FP+FN;
		tp = TP/((double) total);
		fp = FP/((double) total);
		tn = TN/((double) total);
		fn = FN/((double) total);
	}
	
	public ConfusionMatrix(double TPR, double TNR, double FPR, double FNR) {
		total = 1;
		if (TPR+TNR+FPR+FNR != 1) throw new ConstraintViolationException("Sum of paramters must be 1");
		tp = TPR;
		fp = FPR;
		tn = TNR;
		fn = FNR;
	}
	
	public Double sensitivity() {return truePositiveRate();}
	public Double specificity() {return trueNegativeRate();}
	
	
	public Double recall() {return truePositiveRate();}
	public Double precision() {return positivePredictiveValue();}
	
	
	public Double truePositiveRate() {return tp/(tp+fn);}
	public Double trueNegativeRate() {return tn/(tn+fp);}
	public Double falseNegativeRate() {return fn/(fn+tp);}
	public Double falsePositiveRate() {return fp/(fp+tn);}
	
	public Double positivePredictiveValue() {return tp/(tp+fp);}
	public Double negativePredictiveValue() {return tn/(tn+fn);}
	public Double falseDiscoveryRate() {return fp/(fp+tp);}
	public Double falseOmissionRate() {return fn/(fn+tn);}
	
	public Double accuracy() {return (tp+tn)/total;}
	public Double matthewsCorrelationCoefficient() {return (tp*tn-fp*fn)/Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn));}
	public Double fScore(Double beta) {
		Double b2 = Math.pow(beta, 2);
		return ((1+b2)*tp)/((1+b2)*tp+b2*fn+fp);
	}
	public Double f1Score() {return fScore(1D);}
	public Double youdensJ() {return truePositiveRate()+trueNegativeRate()-1;}
	
}
