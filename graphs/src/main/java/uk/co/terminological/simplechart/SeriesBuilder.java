package uk.co.terminological.simplechart;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SeriesBuilder<X> {
	
	X start;
	Function<X,X> next;
	Predicate<X> whileTrue;
	
	public SeriesBuilder(X start) {
		this.start = start;
	}

	public static <Y>  SeriesBuilder<Y> start(Y start) {
		return new SeriesBuilder<Y>(start);
	}
	
	public SeriesBuilder<X> repeat(Function<X,X> fn) {
		this.next = fn;
		return this;
	}
	
	public SeriesBuilder<X> untilFalse(Predicate<X> test) {
		whileTrue = test;
		return this;
	}
	
	public Stream<X> build() {
		Iterator<X> tmp = new Iterator<X>() {

			X nextVal = start;
			
			@Override
			public boolean hasNext() {
				return whileTrue.test(nextVal);
			}

			@Override
			public X next() {
				if (hasNext()) {
					X tmp = nextVal;
					nextVal = next.apply(tmp);
					return tmp;
				}
				throw new NoSuchElementException();
			}
			
		};
		Iterable<X> tmpIt = () -> tmp;
		return StreamSupport.stream(tmpIt.spliterator(),false);
	}
	
	public static Stream<Double> range(Double from, Double to, int divisions) {
		Double increment = (to-from)/divisions;
		return start(from).repeat(d -> d+increment).untilFalse(d -> {
			if (increment>0) return d <= to;
			else if (increment<0) return d >= to;
			else return false;
		}).build();
		
	}
	
	public static Stream<Coordinate> grid(Double xMin, Double xMax, Double yMin, Double yMax, int gridPoints) {
		
	}
}
