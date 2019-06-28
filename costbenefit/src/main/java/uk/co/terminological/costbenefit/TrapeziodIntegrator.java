package uk.co.terminological.costbenefit;

import java.util.List;

import uk.co.terminological.datatypes.Tuple;

public class TrapeziodIntegrator {

	public static Double integrate(List<Tuple<Double,Double>> input) {
		//Sort by first values
		input.sort((t1,t2) -> t1.getFirst().compareTo(t2.getFirst()));
		Double sum = 0D;
		for (int i = 1; i<input.size(); i++) {
			sum += trapeziod(input.get(i-1).getFirst(), input.get(i).getFirst(), input.get(i-1).getSecond(), input.get(i).getSecond());
		}
		return sum;
	}

	private static Double trapeziod(Double x1, Double x2, Double y1, Double y2) {
		return (y1+y2)/2*(x2-x1);
	}
}
