package uk.co.terminological.charts;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
	
	@Override
	public int compareTo(Orderable<X> arg0) {
		return this.getOrder()-arg0.getOrder();
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
