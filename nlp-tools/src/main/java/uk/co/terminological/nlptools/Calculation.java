package uk.co.terminological.nlptools;

public class Calculation {

	public static double pmi(int cooccurAB, int occurrencesOfA, int occurrencesOfB, int totalOccurrences) {
		return pmi(
				((double) cooccurAB)/totalOccurrences,
				((double) occurrencesOfA)/totalOccurrences,
				((double) occurrencesOfB)/totalOccurrences);
	}

	public static double npmi(int cooccurAB, int occurrencesOfA, int occurrencesOfB, int totalOccurrences) {
		return npmi(
				((double) cooccurAB)/totalOccurrences,
				((double) occurrencesOfA)/totalOccurrences,
				((double) occurrencesOfB)/totalOccurrences);
	}
	
	public static double pmi(double pxy, double px, double py) {
		return Math.log(pxy/(px*px));
	}
	
	public static double npmi(double pxy, double px, double py) {
		return Math.log(pxy/(px*px))/(-Math.log(pxy));
	}
	
	public static double mi(int x1y1, int x0y1, int x1y0, int total) {
		int x0y0 = total-(x1y0+x0y1+x1y1);
		double px1 = ((double) x1y0+x1y1)/total;
		double px0 = 1.0-px1;
		double py1 = ((double) x1y1+x0y1)/total;
		double py0 = 1.0-py1;
		double px1y1 = ((double) x1y1)/total;
		double px0y1 = ((double) x0y1)/total;
		double px1y0 = ((double) x1y0)/total;
		double px0y0 = ((double) x0y0)/total;
		return px1y1*pmi(px1y1,px1,py1)
				+px0y1*pmi(px0y1,px0,py1)
				+px1y0*pmi(px1y0,px1,py0)
				+px0y0*pmi(px0y0,px0,py0);
		
	}
}
