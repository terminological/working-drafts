package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.math3.fitting.SimpleCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class ClassifierResult {

	List<Prediction> predictions = new ArrayList<>();
	int totalPositive = 0;
	int total = 0;
	
	public void add(Prediction p) {
		this.predictions.add(p);
		total +=1;
		totalPositive += p.getActual() ? 1:0;
	}

	public List<Prediction> getPredictions() {
		List<Prediction> tmp = new ArrayList<>(predictions);
		tmp.sort(Comparator.comparing(Prediction::getPredicted));
		return tmp;
	}

	public int totalPositive() {
		return totalPositive;
	}

	public int total() {
		return total;
	}
	
	public int totalNegative() {
		return total-totalPositive;
	}

	/**
	 * generates a list of classifier performance metrics based on a sampling resolution
	 * @return
	 */
	public Cutoff.List getCutoffs(Double resolution) {
		PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
		Cutoff c = null;
		Cutoff.List out = new Cutoff.List(resolution);

		int predNeg = 0;
		int falseNeg = 0;

		for (Double i=resolution; i<1D; i+=resolution) {
			while (preds.hasNext() && preds.peek().getPredicted() < i) {
				predNeg += 1;
				falseNeg += preds.next().getActual() ? 1 : 0;
			}
			c = new Cutoff(i, falseNeg, predNeg, totalPositive(), total(), out, out.size());
			out.add(c);
		}
		return out;
	}

	//TODO: This is hairy as hacks Cutoff to deal with situation where there is a single value
	// it would be better to extract things that require fitting into Cutoff.List and make Cutoff and more slimline class
	public Cutoff getValue(Double cutoff) {
		int predNeg = 0;
		int falseNeg = 0;

		for (Prediction pred :this.getPredictions()) {
			 if (pred.getPredicted() <= cutoff) {
				predNeg += 1;
				falseNeg += pred.getActual() ? 1 : 0;
			 }
		}
		Cutoff c = new Cutoff(cutoff, falseNeg, predNeg, totalPositive(), total(), null,0); //out, out.size(), resolution); //replace with "this". resolution can be stored. out.size is position term.
		return c;
	}
	
	public KumaraswamyCDF.Fitted getFittedSensitivity() {
		SimpleCurveFitter fitter = SimpleCurveFitter.create(new KumaraswamyCDF(), new double[] {1D,1D});
		WeightedObservedPoints points = new WeightedObservedPoints();
		
		PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
		int falseNeg = 0;
		
		while (preds.hasNext()) {
			Prediction pred = preds.next();
			while (preds.hasNext() && preds.peek().getPredicted() == pred.getPredicted()) {
				falseNeg += pred.getActual() ? 1 : 0;
				pred = preds.next();
			}
			falseNeg += pred.getActual() ? 1 : 0;
			points.add(pred.getPredicted(), 1-sensitivity(falseNeg));
		}
		// this.getCutoffs(0.01D).stream().forEach(c -> points.add(c.getValue(), 1-c.sensitivity()));
		
		return new KumaraswamyCDF.Fitted(fitter.fit(points.toList()), true);
	}

	public KumaraswamyCDF.Fitted getFittedSpecificity() {
		SimpleCurveFitter fitter = SimpleCurveFitter.create(new KumaraswamyCDF(), new double[] {1D,1D});
		WeightedObservedPoints points = new WeightedObservedPoints();
		
		PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
		int trueNeg = 0;
		
		while (preds.hasNext()) {
			Prediction pred = preds.next();
			while (preds.hasNext() && preds.peek().getPredicted() == pred.getPredicted()) {
				trueNeg += pred.getActual() ? 0 : 1;
				pred = preds.next();
			}
			trueNeg += pred.getActual() ? 0 : 1;
			points.add(pred.getPredicted(), specificity(trueNeg));
		}
		
		return new KumaraswamyCDF.Fitted(fitter.fit(points.toList()), false);
	}
	
	public double sensitivity(int falseNeg) {
		// sensitivity = true positive rate = 1 - false negative rate
		return 1D - ((double) falseNeg) / totalPositive();
	}
	
	public double specificity(int trueNeg) {
		// sensitivity = true negative rate
		return ((double) trueNeg) / totalNegative();
	}
}