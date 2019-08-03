package uk.co.terminological.costbenefit;

import java.util.Optional;

import uk.co.terminological.simplechart.SeriesBuilder;

public interface CostModel {
	Double tpValue(); 
	Double tnValue();
	Double fpCost();
	Double fnCost();
	
	default Optional<Kumaraswamy> minimumNeeded(Double prev) {
		for (Double d=0D;d<=1.0;d+=0.001) {
			Kumaraswamy k = new Kumaraswamy(d,"min");
			Double maxValue = k.bestCutoff(prev, m -> m.normalisedValue(this)).getSecond();
			if (maxValue>0) return Optional.of(k);
		}
		return Optional.empty();
		//TODO: this just looks for the answer in the middle of a balanced classifier.
		// Need to explore the space somehow. skew and divergence as 2D space with best value as 3D
	}
}