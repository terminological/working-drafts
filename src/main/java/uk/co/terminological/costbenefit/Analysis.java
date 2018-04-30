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
import org.knowm.xchart.internal.chartpart.Chart;

import com.google.common.collect.Lists;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {



	public static void main(String[] args) throws ParserException, IOException {

		Path input = Paths.get(args[0]);

		Delimited in = Delimited.fromFile(input.toFile()).tsv().noIdentifiers().withLabels("actual","predicted","prob_pos","prob_neg").begin();
		EavMap<String,String,String> tmp = in.getContents();

		ClassifierResult res = new ClassifierResult();

		tmp.streamEntities()
		.map(kv -> kv.getValue())
		.map(m -> new Prediction(
				convert01TF.apply(m.get("actual")),
				Double.parseDouble(m.get("prob_pos"))))
		.forEach(p -> res.add(p));;

		List<Cutoff> binned = res.getCutoffs(0.01D);
		
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
		for (Chart chart: charts) {
			BitmapEncoder.saveBitmapWithDPI(chart, "/home/rc538/tmp/"+chart.getTitle(), BitmapFormat.PNG, 300);
		}
		
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
		
		for (Chart chart: charts) {
			BitmapEncoder.saveBitmapWithDPI(chart, "/home/rc538/tmp/"+chart.getTitle(), BitmapFormat.PNG, 300);
		}
		
		List<Double> costs = Arrays.asList(-10D,-5D,-2.5D,-1D,0D,2.5D,5D,10D);
		
		for (Double prevalence: Arrays.asList(0.5D,0.1D,0.01D,0.001D,0.0001D)) {
			
			Double valueTP = 1D;
			Double valueFN = -1D;
			Double valueFP = -1D;
			Double valueTN = 1D;
			
			String name = StringUtils.joinWith("_",prevalence,valueTP,valueFN,valueFP,valueTN);
			
			XYChart chart = QuickChart.getChart("value_"+name, "cutoff",name,name, 
					Lists.transform(filtered, c->c.getValue()),
					Lists.transform(filtered, c->c.cost(prevalence, valueTP, valueFN, valueFP, valueTN)));
			
			BitmapEncoder.saveBitmapWithDPI(chart, "/home/rc538/tmp/"+chart.getTitle(), BitmapFormat.PNG, 300);
					
			/*for (Double valueTP: costs) {
				
			}*/
		}
		
	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);

	

}





