package uk.co.terminological.nlptools;

public class Calculation {

	public static Double pointwiseMutualInformation(Integer cooccurAB, Integer occurrencesOfA, Integer occurrencesOfB, Integer totalOccurrences) {
		return Math.log(((double) cooccurAB*totalOccurrences) / (occurrencesOfA*occurrencesOfB));
	}

	public static Double normalisedPointwiseMutualInformation(Integer cooccurAB, Integer occurrencesOfA, Integer occurrencesOfB, Integer totalOccurrences) {
		return -pointwiseMutualInformation(cooccurAB, occurrencesOfA, occurrencesOfB, totalOccurrences) / Math.log(((double) cooccurAB)/totalOccurrences);
	}
	
}
