package uk.co.terminological.nlptools;

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
	
	@Override
	public int compareTo(Weighted<T> o) {
		return -getWeight().compareTo(o.getWeight());
	}
	
}
