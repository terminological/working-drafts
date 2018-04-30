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
		public Double getX() { return getFirst(); }
		public Double getY() { return getSecond(); }
		public String toString() {return "("+getX()+","+getY()+")";}
	}
	
	public static class Result {
		public List<Coordinate> getMinima() {
			return minima;
		}
		public List<Coordinate> getMaxima() {
			return maxima;
		}
		public Coordinate getMax() {
			return max;
		}
		public Coordinate getMin() {
			return min;
		}
		List<Coordinate> minima = new ArrayList<>();
		List<Coordinate> maxima = new ArrayList<>();
		Coordinate max = null;
		Coordinate min = null;
		
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
		
		for (int i=0; i<firstOrder.size(); i++) {
			if (out.min == null || out.min.getY() > coords.get(i)) out.min = Coordinate.create(i*spacing,coords.get(i));
			if (out.max == null || out.max.getY() < coords.get(i)) out.max = Coordinate.create(i*spacing,coords.get(i));
		}
		
		
		for (int i=1; i<firstOrder.size(); i++) {
			
			Double last = firstOrder.get(i-1);
			Double current = firstOrder.get(i);
			
			if (last < 0 && current >= 0) {
				// a local minimum
				Double x = last/(last-current);
				Double yLast = coords.get(i-1);
				Double yCurrent = coords.get(i);
				Double y = Math.min(yLast,yCurrent);
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
