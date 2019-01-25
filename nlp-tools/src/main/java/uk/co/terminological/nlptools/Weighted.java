package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;

public class Weighted<T> implements Comparable<Weighted<T>> {

	

	Double weight;
	T thing;
	
	public Double getWeight() {return weight;}
	public T getTarget() {return thing;}
	
	public Weighted(T thing, Double d) {
		this.thing = thing;
		this.weight = d;
	}
	
	public static <X> Weighted<X> create(X thing, Double d) {
		return new Weighted<X>(thing, d);
	}
	
	public String toString() {return thing.toString()+"(:"+weight.toString()+")";}
	
	public static <X> SortedSet<Weighted<X>> descending() {
		return new TreeSet<Weighted<X>>();
	}
	
	@Override
	public int compareTo(Weighted<T> o) {
		int comp = -getWeight().compareTo(o.getWeight());
		return comp == 0 ? hashCode()-o.hashCode() : comp;
	}
	
}
