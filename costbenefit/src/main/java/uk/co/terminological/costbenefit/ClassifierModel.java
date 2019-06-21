package uk.co.terminological.costbenefit;

public abstract class ClassifierModel<X> {

	public abstract ConfusionMatrix matrix(X param);
	
	Double prevalence = 0.2;
	
	public static class AlwaysPositive extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix matrix(Void param) {
			
			Double tp = prevalence;
			Double tn = 0D;
			Double fp = 1-prevalence;
			Double fn = 0D;
			
			return new ConfusionMatrix(tp,tn,fp,fn);
		}

		
	}
	
	public static class Kumaraswamy extends ClassifierModel<Double> {
		
		Double spreadPos = 0.5;
		Double modePos = 0.9;
		Double spreadNeg = 0.8;
		Double modeNeg = 0.7;
		Double prev = 0.1;
		
		public ConfusionMatrix matrix(Double cutoff) {
			
			Double cdfPos = KumaraswamyCDF.cdf(KumaraswamyCDF.a(spreadPos, modePos),KumaraswamyCDF.a(spreadPos, modePos)).apply(cutoff);
			Double cdfNeg = KumaraswamyCDF.cdf(KumaraswamyCDF.a(spreadNeg, modeNeg),KumaraswamyCDF.a(spreadNeg, modeNeg)).apply(cutoff);
			
			Double eTp = prev*(1 - cdfPos);
			Double eTn = (1-prev)*cdfNeg;
			Double eFp = prev*cdfPos;
			Double eFn = (1-prev)*1-cdfPos;
			
			//TODO: prevalence is independent of cutoff - this should be correct. Classifier model accounts for prevalence.
			
			return new ConfusionMatrix(eTp,eTn,eFp,eFn);
		}
	}
	
	public static class AlwaysNegative extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix matrix(Void param) {
			
			Double tp = 0D;
			Double tn = 1-prevalence;
			Double fp = 0D;
			Double fn = prevalence;
			
			return new ConfusionMatrix(tp,tn,fp,fn);
		}
	}
}
