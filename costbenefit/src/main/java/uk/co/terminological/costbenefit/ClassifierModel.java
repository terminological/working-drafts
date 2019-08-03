package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;

public abstract class ClassifierModel<X> {

	
	public abstract ConfusionMatrix2D matrix(Double prev, X param);
	
	
	
	public static class AlwaysPositive extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix2D matrix(Double prev,Void param) {
			
			Double tp = prev;
			Double tn = 0D;
			Double fp = 1-prev;
			Double fn = 0D;
			
			return new ConfusionMatrix2D(tp,tn,fp,fn);
		}
	}
	
	
	
	public static class AlwaysNegative extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix2D matrix(Double prev,Void param) {
			
			Double tp = 0D;
			Double tn = 1-prev;
			Double fp = 0D;
			Double fn = prev;
			
			return new ConfusionMatrix2D(tp,tn,fp,fn);
		}
	}
	
	
}
