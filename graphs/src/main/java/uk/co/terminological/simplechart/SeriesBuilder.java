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

import org.apache.commons.math3.util.Precision;

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
	
	public static Iterable<Double> iterable(Range range) {
				
		return new Iterable<Double>() {
			
			@Override
			public Iterator<Double> iterator() {
				return new Iterator<Double>() {

					Double value = range.getFirst();
					@Override
					public boolean hasNext() {
						return value<range.getSecond();
					}

					@Override
					public Double next() {
						Double out = value;
						value = value + range.getThird();
						return out;
					}
					
				};
			}
			
		};
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
	
	public static Stream<Double> range(Range rd) {
		return range(rd.getFirst(), rd.getSecond(), rd.getThird());
	}
	
	public static Stream<Double> range(Double from, Double to, Double increment) {
		return start(from).repeat(d -> d+increment).untilFalse(d -> {
			if (increment>0) return d < to || Precision.equals(d, to);
			else if (increment<0) return d > to || Precision.equals(d, to);
			else return false;
		}).build();
	}
	
	public static Stream<Coordinate> grid(Range xRange, Range yRange) {
		return space(xRange,yRange).map(arr-> Coordinate.create(arr[0], arr[1]));
	}
	
	public static Stream<Coordinate> grid(Double xMin, Double xMax, Double yMin, Double yMax, int gridPoints) {
		Double increment = Math.sqrt( (Math.abs(xMax - xMin)*Math.abs(yMax - yMin))/gridPoints );
		long xDivs = (long) Math.floor(Math.abs(xMax-xMin)/increment);
		long yDivs = (long) Math.floor(Math.abs(yMax-yMin)/increment);
		return grid(Range.of(xMin,xMax,increment),Range.of(yMin,yMax,increment));
	}
	
	public static Stream<double[]> space(Range... definitions) {
		if (definitions.length == 0) return Stream.empty();
		final int size = definitions.length; 
		Stream<double[]> out = null;
		int i = 0;
		for (Range rd: definitions) {
			
			int level = i;
			if (i == 0) {
				out = range(rd).map(d -> {
					double[] l = new double[size];
					l[level]=d;
					return l;
				});
			} else {
				out = out.flatMap(l -> range(rd).map(d -> {
					l[level]=d;
					return l;
				}));
			}
			
			i++;
		}
		return out;
	}
	
	public static class Range extends Triple<Double,Double,Double> implements Iterable<Double> {

		private Range(Double item1, Double item2, Double item3) {
			super(item1, item2, item3);
		}
		
		public static Range of(Double from, Double to, Double increment) {
			return new Range(from,to,increment);
		}
		
		public static Range of(Double from, Double to, Long values) {
			return new Range(from,to,(to-from)/(values-1));
		}
		
		public static Range of(Double from, Double to, Integer values) {
			return of(from,to,values.longValue());
		}
		
		public Stream<Double> stream() {
			return SeriesBuilder.range(this);
		}

		@Override
		public Iterator<Double> iterator() {
			return SeriesBuilder.iterable(this).iterator();
		}
		
		
	}

	
	
	
}
