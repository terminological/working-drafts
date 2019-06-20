package uk.co.terminological.costbenefit;

public class ConfusionMatrix {

	Double tpr;
	Double tnr;
	Double fpr;
	Double fnr;
	
	int total;
	
	public ConfusionMatrix(int TP, int TN, int FP, int FN) {
		total = TP+TN+FP+FN;
		tpr = TP/((double) total);
		fpr = FP/((double) total);
		tnr = TN/((double) total);
		fnr = FN/((double) total);
	}
	
	public ConfusionMatrix(double TPR, double TNR, double FPR, double FNR) {
		total = 1;
		if (TPR+TNR+FPR+FNR != 1) throw new ConstraintViolationException("Sum of paramters must be 1");
		tpr = TPR;
		fpr = FPR;
		tnr = TNR;
		fnr = FNR;
	}
	
}
