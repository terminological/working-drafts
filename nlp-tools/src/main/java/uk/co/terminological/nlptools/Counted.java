package uk.co.terminological.nlptools;

import java.util.SortedSet;
import java.util.TreeSet;

public class Counted<T> {

	

	Double weight;
	T thing;
	
	public Double getWeight() {return weight;}
	public T getTarget() {return thing;}
	
	public Counted(T thing, Double d) {
		this.thing = thing;
		this.weight = d;
	}
	
	public static <X> Counted<X> create(X thing, Double d) {
		return new Counted<X>(thing, d);
	}
	
	public String toString() {return thing.toString()+"(:"+weight.toString()+")";}
	
	public static <X> SortedSet<Counted<X>> descending() {
		return new TreeSet<Counted<X>>(
				(w1,w2) -> {
					int comp = -w1.getWeight().compareTo(w2.getWeight());
					return comp == 0 ? w1.hashCode()-w2.hashCode() : comp;
				});
	}
	
	public static <X> SortedSet<Counted<X>> ascending() {
		return new TreeSet<Counted<X>>((w1,w2) -> {
			int comp = w1.getWeight().compareTo(w2.getWeight());
			return comp == 0 ? w1.hashCode()-w2.hashCode() : comp;
		});
	}
	
}
