package uk.co.terminological.charts;

import java.util.List;

public interface DataBound<X> {

	void bind(List<X> data);
	List<X> getData();
	
}
