package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler.LegendPosition;

import com.google.common.collect.Lists;

import uk.co.terminological.costbenefit.CoordinateFinder.Coordinate;
import uk.co.terminological.costbenefit.CoordinateFinder.Inflexions;
import uk.co.terminological.costbenefit.CoordinateFinder.Interceptions;
import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {



	public static void main(String[] args) throws ParserException, IOException {

		Path input = Paths.get(args[0]);
		Path output = Paths.get(args[1]);

		Delimited in = Delimited.fromFile(input.toFile()).tsv().noIdentifiers().withLabels("actual","predicted","prob_pos","prob_neg").begin();
		EavMap<String,String,String> tmp = in.getContents();

		ClassifierResult res = new ClassifierResult();

		tmp.streamEntities()
		.map(kv -> kv.getValue())
		.map(m -> new Prediction(
				convert01TF.apply(m.get("actual")),
				Double.parseDouble(m.get("prob_pos"))))
		.forEach(p -> res.add(p));;

		List<Cutoff> binned = res.getCutoffs();
		
		System.out.println(Cutoff.columns());
		binned.stream().forEach(System.out::println);
		
		//int i=0;
		//while (binned.get(i).deltaFOverGPrime() < 0) i++;
		//Double xIntercept = (binned.get(i-1).deltaFOverGPrime()*binned.get(i-1).getValue()+binned.get(i).deltaFOverGPrime()*binned.get(i).getValue()) / 
		//		(binned.get(i-1).getValue()+binned.get(i).getValue());

		

		
		
		
		
		List<XYChart> charts = new ArrayList<XYChart>();
		
		charts.add(QuickChart.getChart("A", "cutoff", "sensitivity","g(x)",
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.smoothedSensitivity())));
	    
		charts.add(QuickChart.getChart("B", "cutoff", "specificity","specificity", 
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.specificity())));
	    
		charts.add(QuickChart.getChart("C", "1-sensitivity", "specificity","roc", 
				Lists.transform(binned, c->1-c.sensitivity()),
				Lists.transform(binned, c->c.specificity())));
	    
		charts.add(QuickChart.getChart("D", "cutoff", "probability density","f(x)", 
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.smoothedProbabilityDensity())));
	    
		//new SwingWrapper<XYChart>(charts).displayChartMatrix();
		/*for (Chart chart: charts) {
			BitmapEncoder.saveBitmapWithDPI(chart, output.resolve(chart.getTitle()).toString(), BitmapFormat.PNG, 300);
		}*/
		
		// List<XYChart> charts2 = new ArrayList<XYChart>();
				
		charts.add(QuickChart.getChart("E", "cutoff", "first derivative sensitivity","g'(x)", 
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.deltaSensitivity())));
	    
		List<Cutoff> filtered = binned.stream().filter(c -> c.probabilityDensityOverDeltaSensitivity() < 0 && c.probabilityDensityOverDeltaSensitivity() > -1000).collect(Collectors.toList());
		
		charts.add(QuickChart.getChart("F", "cutoff", "f(x)/g'(x)","f(x)/g'(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.smoothedFOverGPrime())));
	    
		charts.add(QuickChart.getChart("G", "cutoff", "g'(x)/f(x)","g'(x)/f(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->1/c.smoothedFOverGPrime())));
		
		charts.add(QuickChart.getChart("H", "cutoff", "value", "value", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.cost(0.1D, 100D, -1D, -10D, 11D))));
				
		
		for (Chart<?,?> chart: charts) {
			BitmapEncoder.saveBitmapWithDPI(chart, output.resolve(chart.getTitle()).toString(), BitmapFormat.PNG, 300);
		}
		
		List<Double> costs = Arrays.asList(-10D,-5D,-2.5D,-1D,-0.1D,0D);
		
		XYChart chart = new XYChartBuilder().width(1200).height(1200).title("Varying prevalences").xAxisTitle("X").yAxisTitle("Y").build();
		 
	    // Customize Chart
	    chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
	    
	    for (Double prevalence: Arrays.asList(0.5D,0.4D,0.3D,0.2D,0.1D,0.05D,0.01D,0.001D)) {
			
			Double valueTP = 100D;
			Double valueFN = -1D;
			Double valueFP = -10D;
			Double valueTN = 11D;
			
			Double kappa = prevalence*(1-(valueTP-valueFP)/(valueTN+valueFN));
			
			chart.addSeries("f(x)/g'(x)="+kappa, 
					Lists.transform(filtered, c->c.getValue()),
					Lists.transform(filtered, c->c.cost(prevalence, valueTP, valueFN, valueFP, valueTN)));
			
		}
		
		BitmapEncoder.saveBitmapWithDPI(chart, output.resolve(chart.getTitle()).toString(), BitmapFormat.PNG, 300);
		
		XYChart chart2 = new XYChartBuilder().width(1200).height(1200).title("Varying incorrect costs").xAxisTitle("X").yAxisTitle("Y").build();
		chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
		
		for (Double cost: costs) {
			
			Double prevalence = 0.1D;
			Double valueTP = 1D;
			Double valueFN = cost;
			Double valueFP = cost;
			Double valueTN = 1D;
			
			chart2.addSeries(cost.toString(),
					Lists.transform(filtered, c->c.getValue()),
					Lists.transform(filtered, c->c.cost(prevalence, valueTP, valueFN, valueFP, valueTN)));
			
		}
		BitmapEncoder.saveBitmapWithDPI(chart2, output.resolve(chart2.getTitle()).toString(), BitmapFormat.PNG, 300);
		
		Inflexions inf = CoordinateFinder.inflexion(Lists.transform(binned, c->c.smoothedFOverGPrime()), res.getResolution());
		System.out.println("==== f(x)/g'(x) inflexions =====");
		System.out.println(inf);
		
		List<Double> xAxis = new ArrayList<>();
		List<Cutoff> yAxis = new ArrayList<>();
		
		
		for (Double kappa = inf.getMin().getY()-1; kappa < inf.getMax().getY()+1; kappa+=(inf.getMax().getY()-inf.getMin().getY()+2)/1000) {
			Interceptions inter = CoordinateFinder.intercept(kappa, 
					Lists.transform(binned, c->c.smoothedFOverGPrime()), res.getResolution());
			for (Coordinate coord: inter.getIntercepts()) {
				Cutoff c = res.getValue(coord.getX());
				xAxis.add(kappa);
				yAxis.add(c);
			}
		}
		
		XYChart chart3 = new XYChartBuilder().width(1200).height(1200).title("Varying kappa").xAxisTitle("kappa").yAxisTitle("Y").build();
		chart3.getStyler().setLegendPosition(LegendPosition.InsideNW);
		chart3.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
	    
		chart3.addSeries("sensitivity",xAxis,Lists.transform(yAxis, c->c.sensitivity()));
		chart3.addSeries("specificity",xAxis,Lists.transform(yAxis, c->c.specificity()));
		chart3.addSeries("cum prob",xAxis,Lists.transform(yAxis, c->c.cumulativeProbability()));
		
		BitmapEncoder.saveBitmapWithDPI(chart3, output.resolve(chart3.getTitle()).toString(), BitmapFormat.PNG, 300);
	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);

	

}





