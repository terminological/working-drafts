package uk.co.terminological.nlptools;

public class Weighted<T> implements Comparable<Weighted<T>> {

	@Override
	public int hashCode() {
		return thing.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Weighted<T> other = (Weighted<T>) obj;
		if (thing == null) {
			if (other.thing != null)
				return false;
		} else if (!thing.equals(other.thing))
			return false;
		return true;
	}

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
	
	public String toString() {return thing.toString()+"(:"+weight.toString()+")";}
	
}
