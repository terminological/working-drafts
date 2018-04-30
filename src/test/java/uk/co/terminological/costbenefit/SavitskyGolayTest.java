package uk.co.terminological.costbenefit;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SavitskyGolayTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTailed() {
		List<Double> tmp2 = Arrays.asList(0D,1D,2D,3D,4D,5D,6D);
		Iterator<List<Double>> tmp3 = SavitzkyGolay.tailed(tmp2,5);
		System.out.println("Tailed: "+tmp2);
		while (tmp3.hasNext()) {
			System.out.println(tmp3.next());
		}	
		assertEquals(SavitzkyGolay.tailed(tmp2,5,0), Arrays.asList(0D,0D,0D,1D,2D));
		assertEquals(SavitzkyGolay.tailed(tmp2,5,6), Arrays.asList(4D,5D,6D,6D,6D));
	}

	@Test
	public void testCircular() {
		List<Double> tmp2 = Arrays.asList(0D,1D,2D,3D,4D,5D,6D);
		Iterator<List<Double>> tmp3 = SavitzkyGolay.circular(tmp2,5);
		System.out.println("Circular: "+tmp2);
		while (tmp3.hasNext()) {
			System.out.println(tmp3.next());
		}	
		assertEquals(SavitzkyGolay.circular(tmp2,5,0), Arrays.asList(5D,6D,0D,1D,2D));
		assertEquals(SavitzkyGolay.circular(tmp2,5,6), Arrays.asList(4D,5D,6D,0D,1D));
	}
	
	@Test
	public void testSymmetric() {
		List<Double> tmp2 = Arrays.asList(0D,1D,2D,3D,4D,5D,6D);
		Iterator<List<Double>> tmp3 = SavitzkyGolay.symmetric(tmp2,5);
		System.out.println("Symmetric: "+tmp2);
		while (tmp3.hasNext()) {
			System.out.println(tmp3.next());
		}	
		assertEquals(SavitzkyGolay.symmetric(tmp2,5,0), Arrays.asList(2D,1D,0D,1D,2D));
		assertEquals(SavitzkyGolay.symmetric(tmp2,5,6), Arrays.asList(4D,5D,6D,5D,4D));
	}
	
	@Test
	public void testFilter() {
		System.out.println(Arrays.toString(SavitzkyGolay.smooth_5_cubic()));
		System.out.println(Arrays.toString(SavitzkyGolay.filter(5,3,0,0.01D)));
	}
}
