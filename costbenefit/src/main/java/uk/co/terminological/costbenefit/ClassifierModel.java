package uk.co.terminological.costbenefit;

public abstract class ClassifierModel<X> {

	public abstract ConfusionMatrix matrix(X param);
	
	Double prevalence = 0.2;
	
	public static class AlwaysPositive extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix matrix(Void param) {
			// TODO Auto-generated method stub
			return null;
		}

		
	}
	
	public static class Kurasawamy extends ClassifierModel<Double> {
		
		Double spreadPos = 0.5;
		Double modePos = 0.9;
		Double spreadNeg = 0.8;
		Double modeNeg = 0.7;
		
		public ConfusionMatrix matrix(Double cutoff) {
			return null;
		}
	}
	
	public static class AlwaysNegative extends ClassifierModel<Void> {

		@Override
		public ConfusionMatrix matrix(Void param) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
