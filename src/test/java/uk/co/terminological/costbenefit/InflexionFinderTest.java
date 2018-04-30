package uk.co.terminological.costbenefit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.costbenefit.CoordinateFinder.Result;

public class InflexionFinderTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		List<Double> coords = Arrays.asList(2D,3D,4D,4.5D,4.6D,4.5D, 4D, 3.9D, 4D,5D, 6D);
		
		List<Double> firstOrder = SavitzkyGolay.convolute(coords, SavitzkyGolay.derivative_5_quad(1D), false);
		
		System.out.println(firstOrder);
		
		Result r = CoordinateFinder.find(
				coords
				, 1D);
		System.out.print(r.toString());
		;
	}

}
