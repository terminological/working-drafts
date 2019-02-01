package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;

public class Counted<T> implements Comparable<Counted<T>> {

	

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
		return new TreeSet<Counted<X>>();
	}
	
	@Override
	public int compareTo(Counted<T> o) {
		int comp = -getCount().compareTo(o.getCount());
		return comp == 0 ? hashCode()-o.hashCode() : comp;
	}
	
	
	
}
