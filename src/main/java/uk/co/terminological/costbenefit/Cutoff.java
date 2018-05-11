package uk.co.terminological.costbenefit;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Cutoff {

	Double value;
	Integer falseNegatives;
	Integer predictedNegatives;
	Integer totalPositives;
	Integer total;
	List<Cutoff> all;
	int index;
	Double resolution;

	public Cutoff(Double value, Integer falseNegatives, Integer predictedNegatives, Integer totalPositives, Integer total, List<Cutoff> all, int index, Double resolution) {
		super();
		this.value = value;
		this.falseNegatives = falseNegatives;
		this.predictedNegatives = predictedNegatives;
		this.total = total;
		this.totalPositives = totalPositives;
		this.all = all;
		this.index = index;
		this.resolution = resolution;
	}
	
	public static String columns() {
		return StringUtils.joinWith("\t", "getValue", "tp","fp","fn","tn","sensitivity","specificity", "smoothedSensitivity","deltaSensitivity","cumulativeProbability",
				"probabilityDensity","smoothedProbabilityDensity","probabilityDensityOverDeltaSensitivity","deltaFOverGPrime");
	}
	
	public String toString() {
		return StringUtils.joinWith("\t", getValue(), tp(),fp(),fn(),tn(),sensitivity(),specificity(), smoothedSensitivity(),deltaSensitivity(),cumulativeProbability(),
				probabilityDensity(),smoothedProbabilityDensity(),probabilityDensityOverDeltaSensitivity(),deltaFOverGPrime());
	}
	
	// ======= Getter methods =========
	 
	public Double getValue() {
		return value;
	}

	public Integer fn() {
		return falseNegatives;
	}

	public Integer tn() {
		return predictedNegatives-falseNegatives;
	}

	public Integer tp() {
		return totalPositives-falseNegatives;
	}

	public Integer fp() {
		return (total-predictedNegatives)-tp();
	}

	public Double sensitivity() {
		return ((double) tp())/totalPositives;
	}

	public Double specificity() {
		return ((double) tn())/(total-totalPositives);
	}

	Double smoothedGX = null;
	public Double smoothedSensitivity() {
		if (smoothedGX == null) smoothedGX = SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_N_sliding(13), false, index, c -> c.sensitivity());
		return smoothedGX;
	}
	
	Double deltaGX = null;
	public Double deltaSensitivity() {
		if (deltaGX == null) deltaGX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(13, 6, 1, resolution), false, index, c -> c.smoothedSensitivity());
		return deltaGX;
	}

	Double smoothedHX = null;
	public Double smoothedSpecificity() {
		if (smoothedHX == null) smoothedHX = SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_N_sliding(13), false, index, c -> c.specificity());
		return smoothedHX;
	}
	
	Double deltaHX = null;
	public Double deltaSpecificity() {
		if (deltaHX == null) deltaHX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(13, 6, 1, resolution), false, index, c -> c.smoothedSpecificity());
		return deltaHX;
	}
	
	public Double cumulativeProbability() {
		return ((double) predictedNegatives)/total;
	}
	
	public Double probabilityDensity() {
		if (index == 0) return cumulativeProbability();
		return ((double) (predictedNegatives-all.get(index-1).predictedNegatives))/total/resolution;
	}
	
	Double smoothPD = null;
	public Double smoothedProbabilityDensity() {
		if (smoothPD == null) smoothPD = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_7_cubic(), false, index, c -> c.probabilityDensity());
		return smoothPD;
	}
	
	public Double probabilityDensityOverDeltaSensitivity() {
		return probabilityDensity()/deltaSensitivity(); 
	}
	
	public Double smoothedFOverGPrime() {
		return SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_N_sliding(13), false, index, c -> c.probabilityDensityOverDeltaSensitivity());
	}
	
	public Double deltaFOverGPrime() {
		return SavitzkyGolay.convolute(all, SavitzkyGolay.derivative_5_quad(resolution), false, index, c -> c.smoothedFOverGPrime()); 
	}
	
	public Double cost(Double prevalence, Double valueTP, Double valueFN, Double valueFP, Double valueTN) {
		return valueTP * prevalence * smoothedSensitivity() +
				valueFN * (1- cumulativeProbability() - prevalence * smoothedSensitivity()) +
				valueFP * (prevalence - smoothedSensitivity()*prevalence) +
				valueTN * (prevalence + cumulativeProbability() - smoothedSensitivity()*prevalence);
	}
	
}