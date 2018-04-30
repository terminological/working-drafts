package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.terminological.datatypes.Tuple;

public class InflexionFinder {

	public static class Coordinate extends Tuple<Double,Double> {

		public Coordinate(Double item1, Double item2) {
			super(item1, item2);
		}
		public static Coordinate create(Double x,Double y) {
			return new Coordinate(x,y);
		}
	}
	
	public static class Result {
		public List<Coordinate> getMinima() {
			return minima;
		}
		public List<Coordinate> getMaxima() {
			return maxima;
		}
		public Double getMax() {
			return max;
		}
		public Double getMin() {
			return min;
		}
		List<Coordinate> minima = new ArrayList<>();
		List<Coordinate> maxima = new ArrayList<>();
		Double max = null;
		Double min = null;
		
		public String toString() {
			return "range: "+min+" - "+max+"\nminima: \n"
					+getMinima().stream().map(c -> c.toString()).collect(Collectors.joining("\n"))
					+"\nmaxima: \n"
					+getMaxima().stream().map(c -> c.toString()).collect(Collectors.joining("\n"))
					;
		}
	}
	
	public static Result find(List<Double> coords, Double spacing) {
		
		Result out = new Result();
		
		List<Double> firstOrder = SavitzkyGolay.convolute(coords, SavitzkyGolay.derivative_5_quad(spacing), false);
		
		for (int i=1; i<firstOrder.size(); i++) {
			
			
			if (out.min == null || out.min > coords.get(i)) out.min = coords.get(i);
			if (out.max == null || out.max < coords.get(i)) out.max = coords.get(i);
			
			Double last = firstOrder.get(i-1);
			Double current = firstOrder.get(i);
			/*if (current == 0) {
				// current = firstOrder.get(i+1);
				// not dealing with saddles
				if (last < 0) {
					
				}
			}*/
			
			if (last < 0 && current >= 0) {
				// a local minimum
				Double x = last/(last-current);
				Double yLast = coords.get(i-1);
				Double yCurrent = coords.get(i);
				Double y = Math.max(yLast,yCurrent);
				out.minima.add(Coordinate.create(((i-1)+x)/spacing, y));
				
			}
			
			if (last > 0 && current <= 0) {
				// a local maximum
				Double x = last/(last-current);
				Double yLast = coords.get(i-1);
				Double yCurrent = coords.get(i);
				Double y = Math.max(yLast,yCurrent);
				out.maxima.add(Coordinate.create(((i-1)+x)/spacing, y));
			}
		}
		
		return out;
	}
	
}
