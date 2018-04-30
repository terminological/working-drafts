package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.google.common.collect.Lists;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {



	public static void main(String[] args) throws FileNotFoundException, ParserException {

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
	    
		new SwingWrapper<XYChart>(charts).displayChartMatrix();
		
		List<XYChart> charts2 = new ArrayList<XYChart>();
				
		charts2.add(QuickChart.getChart("E", "cutoff", "first derivative sensitivity","g'(x)", 
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.deltaSensitivity())));
	    
		List<Cutoff> filtered = binned.stream().filter(c -> c.probabilityDensityOverDeltaSensitivity() < 0 && c.probabilityDensityOverDeltaSensitivity() > -1000).collect(Collectors.toList());
		
		charts2.add(QuickChart.getChart("F", "cutoff", "f(x)/g'(x)","f(x)/g'(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.smoothedFOverGPrime())));
	    
		charts2.add(QuickChart.getChart("G", "cutoff", "g'(x)/f(x)","g'(x)/f(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->1/c.smoothedFOverGPrime())));
		
		filtered = binned.stream().filter(c -> c.deltaFOverGPrime() < 10 && c.deltaFOverGPrime() > -10).collect(Collectors.toList());
		
		charts2.add(QuickChart.getChart("H", "cutoff", "f(x)/g'(x)","deriv f(x)/g'(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.deltaFOverGPrime())
				));
		
	    new SwingWrapper<XYChart>(charts2).displayChartMatrix();
		
	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);

	

}





