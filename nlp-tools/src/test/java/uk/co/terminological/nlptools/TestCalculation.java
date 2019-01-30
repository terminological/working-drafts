package uk.co.terminological.nlptools;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCalculation {

	@Test
	public void testPMI() {
		double pmi = Calculation.pmi(1159, 1938, 1311, 50000952);
		System.out.println(pmi);
		assert(Math.abs(pmi-10.0349081703D) < 0.0001 );
	}

}
