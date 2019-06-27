package uk.co.terminological.simplechart;

import uk.co.terminological.datatypes.Tuple;

public class Coordinate extends Tuple<Double,Double> {

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