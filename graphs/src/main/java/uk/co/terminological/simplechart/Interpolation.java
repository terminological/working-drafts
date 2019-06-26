package uk.co.terminological.simplechart;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.analysis.interpolation.MicrosphereProjectionInterpolator;

import uk.co.terminological.simplechart.Interpolator.Interpolation;

public class Interpolation<IN> {

	public static <Y> Interpolation<Y> empty() {
		return new Interpolation<Y>();
	}

	public MicrosphereProjectionInterpolator interp;
	public List<Function<IN, Double>> adaptors;
	
	
}