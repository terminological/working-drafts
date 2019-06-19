package uk.co.terminological.costbenefit;

import static org.junit.Assert.*;
import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;
import static uk.co.terminological.simplechart.Chart.Dimension.Y_FIT;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;


import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;

public class ClassifierSimulation {

	
	Figure figures = Figure.outputTo(output.toFile());
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void test() {
		figures.withNewChart("hx", ChartType.XY_LINE)
		.config().withXScale(0F, 1F)
		.withXLabel("cutoff")
		.withYLabel("specificity - h(x)")
		.withYScale(0F, 1F)
		.done()
		.withSeries()
		.bind(X, t -> t.getValue())
		.bind(Y_FIT, t -> fitSpec.value(t.getValue()))//t.smoothedSpecificity())
		.bind(Y, t -> t.specificity())
		.done()
		.render();
		
	}

}
