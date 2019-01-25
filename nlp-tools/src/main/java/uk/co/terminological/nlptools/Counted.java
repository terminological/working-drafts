package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;

public class Counted<T> {

	

	Integer count;
	T thing;
	
	public Integer getCount() {return count;}
	public T getTarget() {return thing;}
	
	public Counted(T thing, Integer d) {
		this.thing = thing;
		this.count = d;
	}
	
	public static <X> Counted<X> create(X thing, Integer d) {
		return new Counted<X>(thing, d);
	}
	
	public String toString() {return thing.toString()+"(:"+count.toString()+")";}
	
	public static <X> SortedSet<Counted<X>> descending() {
		return new TreeSet<Counted<X>>(
				(w1,w2) -> {
					int comp = -w1.getCount().compareTo(w2.getCount());
					return comp == 0 ? w1.hashCode()-w2.hashCode() : comp;
				});
	}
	
	public static <X> SortedSet<Counted<X>> ascending() {
		return new TreeSet<Counted<X>>((w1,w2) -> {
			int comp = w1.getCount().compareTo(w2.getCount());
			return comp == 0 ? w1.hashCode()-w2.hashCode() : comp;
		});
	}
	
}
