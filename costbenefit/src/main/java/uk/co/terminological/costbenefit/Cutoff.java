package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class Cutoff {

	public static class List extends ArrayList<Cutoff> {

		Double resolution;
		public Double getResolution() {return resolution;} 
		public List(Double resolution) {
			super();
			this.resolution = resolution;
		}
		
	}
	
	Double value;
	Integer falseNegatives;
	Integer predictedNegatives;
	Integer totalPositives;
	Integer total;
	List all;
	int index;
	
	public Cutoff(Double value, Integer falseNegatives, Integer predictedNegatives, Integer totalPositives, Integer total, List all, int index) {
		super();
		this.value = value;
		this.falseNegatives = falseNegatives;
		this.predictedNegatives = predictedNegatives;
		this.total = total;
		this.totalPositives = totalPositives;
		this.all = all;
		this.index = index;
		
	}
	
	public static String columns() {
		return StringUtils.joinWith("\t", "getValue", "tp","fp","fn","tn","sensitivity","specificity", "smoothedSensitivity","deltaSensitivity","cumulativeProbability",
				"probabilityDensity","smoothedProbabilityDensity","probabilityDensityOverDeltaSensitivity");
	}
	
	public String toString() {
		return StringUtils.joinWith("\t", getValue(), tp(),fp(),fn(),tn(),sensitivity(),specificity(), smoothedSensitivity(),deltaSensitivity(),cumulativeProbability(),
				probabilityDensity(),smoothedProbabilityDensity(),probabilityDensityOverDeltaSensitivity());
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
		if (smoothedGX == null) smoothedGX = SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 0, all.getResolution()), false, index, c -> c.sensitivity());
		return smoothedGX;
	}
	
	Double deltaGX = null;
	public Double deltaSensitivity() {
		if (deltaGX == null) deltaGX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 1, all.getResolution()), false, index, c -> c.sensitivity());
		return deltaGX;
	}

	Double d2GX = null;
	public Double d2Sensitivity() {
		if (d2GX == null) d2GX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 2, all.getResolution()), false, index, c -> c.sensitivity());
		return d2GX;
	}
	
	Double smoothedHX = null;
	public Double smoothedSpecificity() {
		if (smoothedHX == null) smoothedHX = SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 0, all.getResolution()), false, index, c -> c.specificity());
		return smoothedHX;
	}
	
	Double deltaHX = null;
	public Double deltaSpecificity() {
		if (deltaHX == null) deltaHX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 1, all.getResolution()), false, index, c -> c.specificity()); //?smoothedSpecificity
		return deltaHX;
	}
	
	Double d2HX = null;
	public Double d2Specificity() {
		if (d2HX == null) d2HX = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 2, all.getResolution()), false, index, c -> c.specificity()); //?smoothedSpecificity
		return d2HX;
	}
	
	public Double cumulativeProbability() {
		return ((double) predictedNegatives)/total;
	}
	
	public Double probabilityDensity() {
		if (index == 0) return cumulativeProbability();
		return ((double) (predictedNegatives-all.get(index-1).predictedNegatives))/total/all.getResolution();
	}
	
	Double smoothPD = null;
	public Double smoothedProbabilityDensity() {
		if (smoothPD == null) smoothPD = 
				SavitzkyGolay.convolute(all, SavitzkyGolay.filter(25, 3, 0, all.getResolution()), false, index, c -> c.probabilityDensity());
		return smoothPD;
	}
	
	public Double probabilityDensityOverDeltaSensitivity() {
		return probabilityDensity()/deltaSensitivity(); 
	}
	
	public Double cost(Double prevalence, Double valueTP, Double valueFN, Double valueFP, Double valueTN) {
		return valueFN
				+ (valueFP-valueFN)*prevalence
				+ (valueTN-valueFP)*prevalence*smoothedSensitivity()
				+ (valueTN-valueFN)*prevalence*smoothedSpecificity();
	}
	
	public Double deltaCost(Double prevalence, Double valueTP, Double valueFN, Double valueFP, Double valueTN) {
		return (valueTN-valueFP)*prevalence*deltaSensitivity()
				+ (valueTN-valueFN)*prevalence*deltaSpecificity();
	}
	
	public Double nonDimValue(Double prevalence, Double valueTP, Double valueFN, Double valueFP, Double valueTN) {
		return (valueTN-valueFN)/(valueTP-valueFP)*prevalence/(1-prevalence);
	}
	
	//I think this is necessary but not sufficient.
	//This function just means there is some value of cutoff that is positive - it doesn't mean every value is.
	// it could also be finding minima
	public Boolean isSolvable(Double prevalence, Double valueTP, Double valueFN, Double valueFP, Double valueTN) {
		Double dValueTotByDx = valueTP*prevalence*deltaSensitivity()-valueFP*prevalence*deltaSensitivity()+valueTN*(1-prevalence)*deltaSpecificity()-valueFN*(1-prevalence)*deltaSpecificity();
		Double d2ValueTotByDx = valueTP*prevalence*d2Sensitivity()-valueFP*prevalence*d2Sensitivity()+valueTN*(1-prevalence)*d2Specificity()-valueFN*(1-prevalence)*d2Specificity();
		return
				(0.1D > dValueTotByDx && dValueTotByDx > -0.1D) 
				&& d2ValueTotByDx < 0
				// && (0 <= (valueFN*(1-prevalence)+valueFP*prevalence)*(deltaSpecificity()+deltaSensitivity())) 
				// && (0 >= (valueTN*(1-prevalence)+valueTP*prevalence)*(deltaSpecificity()+deltaSensitivity()))
				&& (valueFN*(prevalence-1) <= valueTP*(prevalence))
				&& (valueTN*(prevalence-1) <= valueTP*(prevalence))
				&& (valueTN*(prevalence-1) <= valueFP*(prevalence))
				&& (valueFP*(prevalence-1) >= valueFN*(prevalence))
				;
	}
		
	
}