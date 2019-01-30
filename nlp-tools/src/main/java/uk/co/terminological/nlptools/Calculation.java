package uk.co.terminological.nlptools;

public class Calculation {

	public static double pmi(int x1y1, int x1, int occurrencesOfB, int totalOccurrences) {
		return pmi(
				((double) x1y1)/totalOccurrences,
				((double) x1)/totalOccurrences,
				((double) occurrencesOfB)/totalOccurrences);
	}

	public static double npmi(int cooccurAB, int occurrencesOfA, int occurrencesOfB, int totalOccurrences) {
		return npmi(
				((double) cooccurAB)/totalOccurrences,
				((double) occurrencesOfA)/totalOccurrences,
				((double) occurrencesOfB)/totalOccurrences);
	}
	
	public static double pmi(double pxy, double px, double py) {
		if (px==0 || py==0) return 0D; //pxy must also be zero at this point. could return NaN...
		if (pxy == 0) return Double.NEGATIVE_INFINITY;
		return Math.log(pxy/(px*py));
	}
	
	public static double npmi(double pxy, double px, double py) {
		return pmi(pxy,px,py)/(-Math.log(pxy));
	}
	
	public static double mi(int x1y1, int x0y1, int x1y0, int total) {
		double px1y1 = ((double) x1y1)/total;
		double px0y1 = ((double) x0y1)/total;
		double px1y0 = ((double) x1y0)/total;
		double px0y0 = 1.0-(px1y1+px0y1+px1y0);
		return mi(px1y1,px0y1,px1y0,px0y0);
	}
	
	public static double mi(double px1y1, double px0y1, double px1y0, double px0y0) {
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
