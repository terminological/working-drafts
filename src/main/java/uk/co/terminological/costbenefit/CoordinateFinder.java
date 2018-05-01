package uk.co.terminological.costbenefit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.terminological.datatypes.Tuple;

public class CoordinateFinder {

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
	
	public static class Interceptions {
		List<Coordinate> intercepts = new ArrayList<>();
		public List<Coordinate> getIntercepts() {
			return intercepts;
		}
		public String toString() {
			return "intercepts: \t"
					+getIntercepts().stream().map(c -> c.toString()).collect(Collectors.joining("\t"))
					+"\n"
					;
		}
	}
	
	public static class Inflexions {
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
			return "range: "+min+" - "+max+"\nminima: \t"
					+getMinima().stream().map(c -> c.toString()).collect(Collectors.joining("\t"))
					+"\nmaxima: \t"
					+getMaxima().stream().map(c -> c.toString()).collect(Collectors.joining("\t"))
					+"\n"
					;
		}
	}
	
	public static Inflexions inflexion(List<Double> coords, Double spacing) {
		
		Inflexions out = new Inflexions();
		
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
				out.minima.add(Coordinate.create(((i-1)+x)*spacing, y));	
			}
			
			if (last > 0 && current <= 0) {
				// a local maximum
				Double x = last/(last-current);
				Double yLast = coords.get(i-1);
				Double yCurrent = coords.get(i);
				Double y = Math.max(yLast,yCurrent);
				out.maxima.add(Coordinate.create(((i-1)+x)*spacing, y));
			}
		}
		
		return out;
	}
	
	public static Interceptions intercept(Double yValue, List<Double> coords, Double spacing) {
		Interceptions out = new Interceptions();
		
		for (int i=1; i<coords.size(); i++) {
			
			Double last = coords.get(i-1);
			Double current = coords.get(i);
			
			if ((last < yValue && current >= yValue) || (last > yValue && current <= yValue)) {
				// a local minimum
				Double x = (yValue-last)/(current-last);
				out.intercepts.add(Coordinate.create(((i-1)+x)*spacing, yValue));	
			}
			
		}
		
		return out;
	}
	
}
