package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.iterators.PeekingIterator;

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

	public List<Cutoff> getCutoffs(Double resolution) {

		PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
		Cutoff c = null;
		List<Cutoff> out = new ArrayList<>();

		int predNeg = 0;
		int falseNeg = 0;

		for (Double i=0D; i<1+resolution*0.95; i+=resolution) {

			while (preds.hasNext() && preds.peek().getPredicted() < i) {
				predNeg += 1;
				falseNeg += preds.next().getActual() ? 1 : 0;
			}

			c = new Cutoff(i, falseNeg, predNeg, totalPositive(), total(), out, out.size(), resolution);
			out.add(c);

		}

		return out;

	}
	
	public Cutoff getValue(Double cutoff) {
		int predNeg = 0;
		int falseNeg = 0;

		this.getPredictions().stream().filter(p->p.getPredicted()<=cutoff)
			.forEach(pred-> {
				predNeg += 1;
				falseNeg += pred.getActual() ? 1 : 0;
			});

		Cutoff c = new Cutoff(cutoff, falseNeg, predNeg, totalPositive(), total(), null,0,null); //out, out.size(), resolution); //replace with "this". resolution can be stored. out.size is position term.
		return c;
	}

}