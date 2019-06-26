package uk.co.terminological.simplechart;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.analysis.interpolation.MicrosphereProjectionInterpolator;

public class Interpolation<IN> {

	public static <Y> Interpolation<Y> empty() {
		return new Interpolation<Y>(null,null);
	}

	private MicrosphereProjectionInterpolator interp;
	private List<Function<IN, Double>> adaptors;
	
	
	public Interpolation(MicrosphereProjectionInterpolator interp, List<Function<IN, Double>> adaptors) {
		this.interp = interp;
		this.adaptors = adaptors;
	}
	
}