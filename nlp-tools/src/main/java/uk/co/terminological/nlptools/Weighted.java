package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;

public class Weighted<T> {

	

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
		return new TreeSet<Weighted<X>>((w1,w2) -> -w1.getWeight().compareTo(w2.getWeight()));
	}
	
	public static <X> SortedSet<Weighted<X>> ascending() {
		return new TreeSet<Weighted<X>>((w1,w2) -> w1.getWeight().compareTo(w2.getWeight()));
	}
	
}
