package uk.co.terminological.costbenefit;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class Simulator {

	
	
	public Stream<Patient> generate(int n, List<FeatureType> features) {
		return null;
	}
	
	
	public static class Patient {
		boolean diseaseStatus;
		Map<FeatureType,Double> features;
	}
	
	public static enum FeatureType {
		
		;
		Feature feature;
		FeatureType(Feature feature) {
			this.feature = feature;
		}
		Feature get() {return feature;}
	}
	
	public static interface Feature {
		Double sample(boolean diseaseStatus);
	}
	
	public static class BinomialFeature implements Feature {
		private Random rand = new Random();
		public Double featureProbGivenDisease;
		public Double featureProbGivenNoDisease;
		@Override
		public Double sample(boolean givenDisease) {
			if (givenDisease) {
				if (rand.nextDouble() < featureProbGivenDisease) return 1D;
				else return 0D;
			} else {
				if (rand.nextDouble() < featureProbGivenNoDisease) return 1D;
				else return 0D;
			}
		}
		
	}
	
	public static class LogGaussianFeature implements Feature {
		private Random rand = new Random();
		public Double meanGivenDisease;
		public Double varGivenDisease;
		public Double meanGivenNoDisease;
		public Double varGivenNoDisease;
		
		private Double mu(Double x, Double sd) {
			return Math.log((x*x)/Math.sqrt(x*x+sd));
		}
		
		private Double sigma(Double x, Double sd) {
			return Math.sqrt(Math.log(sd/(x*x)+1));
		}
		
		public Double sample(boolean givenDisease) {
			if (givenDisease) {
				return Math.log(rand.nextGaussian()*sigma(meanGivenDisease,varGivenDisease)+mu(meanGivenDisease,varGivenDisease));
			} else {
				return Math.log(rand.nextGaussian()*sigma(meanGivenNoDisease,varGivenNoDisease)+mu(meanGivenNoDisease,varGivenNoDisease));
			}
		}
	}
	
	public static class KumuraswamyFeature implements Feature {

		private Random rand = new Random();
		public Double modeGivenDisease;
		public Double aGivenDisease;
		public Double modeGivenNoDisease;
		public Double aGivenNoDisease;
		
		
		@Override
		public Double sample(boolean givenDisease) {
			if (givenDisease) {
				return KumaraswamyCDF.inv(aGivenDisease,b(aGivenDisease,modeGivenDisease)).apply(rand.nextDouble());
			} else {
				return KumaraswamyCDF.inv(aGivenNoDisease,b(aGivenNoDisease,modeGivenNoDisease)).apply(rand.nextDouble());
			}
		}
		
	}
}
