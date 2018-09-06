package uk.co.terminological.charts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.terminological.charts.Scale.Discrete;

public class Orderable<X> implements Comparable<Orderable<X>> {

	private int order;
	private X value;
	private String label;
	
	public int getOrder() {return order;}
	public String getLabel() {return label;}
	public X getValue() {return value;}
	public void setOrder(int i) {
		this.order = i;
	}
	
	private Orderable() {}
	
	@Override
	public int compareTo(Orderable<X> arg0) {
		return this.getOrder()-arg0.getOrder();
	}
	
	public void addToList(List<Orderable<X>> list) {
		setOrder(list.size());
		list.add(this);
	}
	
	public static <X> Orderable<X> create(X value, String label, Discrete<X> scale) {
		Orderable<X> out = new Orderable<X>();
		out.value = value;
		out.label = label;
		out.addToList(scale.getCategories());
		return out;
	}
	
	public static Orderable<String> create(String label, Discrete<String> scale) {
		Orderable<String> out = new Orderable<String>();
		out.value = label;
		out.label = label;
		out.addToList(scale.getCategories());
		return out;
	}
	
	public static <X> List<Orderable<X>> sortByLabel(List<Orderable<X>> items, Comparator<String> comparator) {
		Collections.sort(items, new Comparator<Orderable<X>>() {
			@Override
			public int compare(Orderable<X> arg0, Orderable<X> arg1) {
				return comparator.compare(arg0.getLabel(), arg1.getLabel());
			}
		});
		int i=0;
		for (Orderable<X> item: items) {
			item.setOrder(i);
			i++;
		}
		return items;
	}

	
	public static <X> List<Orderable<X>> sortByValue(List<Orderable<X>> items, Comparator<X> comparator) {
		Collections.sort(items, new Comparator<Orderable<X>>() {
			@Override
			public int compare(Orderable<X> arg0, Orderable<X> arg1) {
				return comparator.compare(arg0.getValue(), arg1.getValue());
			}
		});
		int i=0;
		for (Orderable<X> item: items) {
			item.setOrder(i);
			i++;
		}
		return items;
	}
	
	
}
