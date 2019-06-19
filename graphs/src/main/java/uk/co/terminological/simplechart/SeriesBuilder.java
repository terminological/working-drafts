package uk.co.terminological.simplechart;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.terminological.datatypes.Triple;

public class SeriesBuilder<X> {
	
	X start;
	UnaryOperator<X> next;
	Predicate<X> whileTrue;
	
	public SeriesBuilder(X start) {
		this.start = start;
	}

	public static <Y>  SeriesBuilder<Y> start(Y start) {
		return new SeriesBuilder<Y>(start);
	}
	
	public SeriesBuilder<X> repeat(UnaryOperator<X> fn) {
		this.next = fn;
		return this;
	}
	
	public SeriesBuilder<X> untilFalse(Predicate<X> test) {
		whileTrue = test;
		return this;
	}
	
	public Stream<X> build() {
		return Stream.iterate(start, whileTrue, next);
	}
	
	public static Stream<Double> range(Double from, Double to, long values) {
		long divisions = Math.abs(values)-1;
		if (divisions < 1) divisions=1;
		Double increment = (to-from)/divisions;
		return range(from,to,increment);
	}
	
	public static Stream<Double> range(RangeDefinition rd) {
		return range(rd.getFirst(), rd.getSecond(), rd.getThird());
	}
	
	public static Stream<Double> range(Double from, Double to, Double increment) {
		return start(from).repeat(d -> d+increment).untilFalse(d -> {
			if (increment>0) return d <= to;
			else if (increment<0) return d >= to;
			else return false;
		}).build();
	}
	
	public static Stream<Coordinate> grid(Double xMin, Double xMax, Double yMin, Double yMax, int gridPoints) {
		Double increment = Math.sqrt( (Math.abs(xMax - xMin)*Math.abs(yMax - yMin))/gridPoints );
		long xDivs = (long) Math.floor(Math.abs(xMax-xMin)/increment);
		long yDivs = (long) Math.floor(Math.abs(yMax-yMin)/increment);
		return range(xMin,xMax,xDivs).flatMap(x -> range(yMin,yMax,yDivs).map(y-> Coordinate.create(x, y)));
	}
	
	public static Stream<List<Double>> space(RangeDefinition... definitions) {
		if (definitions.length == 0) return Stream.empty();
		Stream<List<Double>> out = null;
		for (RangeDefinition rd: definitions) {
			if (out != null) {
				out = out.flatMap(l -> range(rd).map(d -> {
					l.add(d);
					return l;
				}));
			} else {
				out = range(rd).map(d -> {
					List<Double> l = new ArrayList<>();
					l.add(d);
					return l;
				});
			}
		}
		return out;
	}
	
	public static class RangeDefinition extends Triple<Double,Double,Long> {

		private RangeDefinition(Double item1, Double item2, Long item3) {
			super(item1, item2, item3);
		}
		
		public static RangeDefinition of(Double from, Double to, Long values) {
			return new RangeDefinition(from,to,values);
		}
		
	}
	
	
}
