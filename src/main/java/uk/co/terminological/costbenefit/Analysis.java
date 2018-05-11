package uk.co.terminological.costbenefit;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;
import static uk.co.terminological.simplechart.Chart.Dimension.Z;
import static uk.co.terminological.simplechart.Chart.Dimension.Y_FIT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import freemarker.template.TemplateException;
import uk.co.terminological.costbenefit.CoordinateFinder.Coordinate;
import uk.co.terminological.costbenefit.CoordinateFinder.Inflexions;
import uk.co.terminological.costbenefit.CoordinateFinder.Interceptions;
import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.simplechart.Chart;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.tabular.Delimited;

public class Analysis {



	public static void main(String[] args) throws ParserException, IOException, TemplateException {

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

		

		Figure.Data<Cutoff> figures = Figure.outputTo(output.toFile())
				.withDefaultData(binned);
		
		figures.withNewChart("gx", ChartType.XY_LINE)
			.bind(X, t -> t.getValue())
			.bind(Y, t -> t.smoothedSensitivity())
			.bind(Y_FIT, t -> t.sensitivity())
			.withAxes("cutoff","sensitivity - g(x)")
			.config().withXScale(0F, 1F)
			.render();
		
		figures.withNewChart("hx", ChartType.XY_LINE)
			.bind(X, t -> t.getValue())
			.bind(Y, t -> t.smoothedSpecificity())
			.bind(Y_FIT, t -> t.specificity())
			.withAxes("cutoff","specificity - h(x)")
			.config().withXScale(0F, 1F)
			.render();
		
		figures.withNewChart("roc", ChartType.XY_LINE)
			.bind(X, t -> 1-t.sensitivity())
			.bind(Y, t -> t.specificity())
			.withAxes("1-sensitivity","specificity")
			.config().withXScale(0F, 1F)
			.render();
		
		figures.withNewChart("fx", ChartType.XY_LINE)
			.bind(X, t -> t.getValue())
			.bind(Y, t -> t.probabilityDensity())
			.bind(Y_FIT, t->t.smoothedProbabilityDensity())
			.withAxes("cutoff","density - f(x)")
			.config().withXScale(0F, 1F)
			.render();
		
		figures.withNewChart("gprimex", ChartType.XY_LINE)
		.bind(X, t -> t.getValue())
		.bind(Y, t -> t.deltaSensitivity())
		.withAxes("cutoff","rate of change sensitivity: g'(x)")
		.config().withXScale(0F, 1F)
		.render();
	
	figures.withNewChart("hprimex", ChartType.XY_LINE)
		.bind(X, t -> t.getValue())
		.bind(Y, t -> t.deltaSpecificity())
		.withAxes("cutoff","rate of change specificity: h'(x)")
		.config().withXScale(0F, 1F)
		.render();
		
		figures.withNewChart("bigFx", ChartType.XY_LINE)
			.bind(X, t -> t.getValue())
			.bind(Y, t -> t.cumulativeProbability())
			.withAxes("cutoff","cumulative probability: F(x)")
			.config().withXScale(0F, 1F)
			.render();
		
		figures.withNewChart("costExample", ChartType.XY_LINE)
			.bind(X, t -> t.getValue())
			.bind(Y, t -> t.cost(0.35D, 100D, -1D, -10D, 11D))
			.withAxes("cutoff","value")
			.config().withXScale(0F, 1F)
			.render();
		
		//List<Double> costs = Arrays.asList(-10D,-5D,-2.5D,-1D,-0.1D,0D);
		
		Chart<Cutoff> chart = 
				figures.withNewChart("costByPrevalence", ChartType.XY_MULTI_LINE)
				.bind(X, t -> t.getValue())
				.withAxes("cutoff","value");
		chart.config().withXScale(0F, 1F);
		 
	    for (Double prevalence: Arrays.asList(0.5D,0.4D,0.3D,0.2D,0.1D,0.05D,0.01D,0.001D)) {
			
			Double valueTP = 100D;
			Double valueFN = -1D;
			Double valueFP = -10D;
			Double valueTN = 11D;
			
			chart.bind(Y, t -> t.cost(prevalence, valueTP, valueFN, valueFP, valueTN), String.format("%2G", prevalence));
			
		}
		
	    chart.render();
	    
	    
	    figures.withNewChart("costExample", ChartType.XY_LINE)
		.bind(X, t -> t.getValue())
		.bind(Y, t -> t.cost(0.1D, 100D, -1D, -10D, 11D))
		.withAxes("cutoff","value")
		.config().withXScale(0F, 1F)
		.render();
	    
		/*BitmapEncoder.saveBitmapWithDPI(chart, output.resolve(chart.getTitle()).toString(), BitmapFormat.PNG, 300);
		
		XYChart chart2 = new XYChartBuilder().width(1200).height(1200).title("Varying incorrect costs").xAxisTitle("X").yAxisTitle("Y").build();
		chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
		
		for (Double cost: costs) {
			
			Double prevalence = 0.1D;
			Double valueTP = 1D;
			Double valueFN = cost;
			Double valueFP = cost;
			Double valueTN = 1D;
			
			chart2.addSeries(cost.toString(),
					Lists.transform(binned, c->c.getValue()),
					Lists.transform(binned, c->c.cost(prevalence, valueTP, valueFN, valueFP, valueTN)));
			
		}
		BitmapEncoder.saveBitmapWithDPI(chart2, output.resolve(chart2.getTitle()).toString(), BitmapFormat.PNG, 300);
		*/
	    /*
		Inflexions inf = CoordinateFinder.inflexion(Lists.transform(binned, c->c.smoothedFOverGPrime()), res.getResolution());
		System.out.println("==== f(x)/g'(x) inflexions =====");
		System.out.println(inf);*/
		
		List<Tuple<Double,Cutoff>> data = new ArrayList<>();
		
		
		for (double prevalence = 0D; prevalence <1.01D; prevalence += 0.01D) {
			final double tmp2 = prevalence;
			Interceptions inter = CoordinateFinder.intercept(0D, 
					Lists.transform(binned, c->c.deltaCost(tmp2)), res.getResolution());
			for (Coordinate coord: inter.getIntercepts()) {
				Cutoff c = res.getValue(coord.getX());
				data.add(Tuple.create(prevalence,c));
			}
		}
		
		figures.withNewChart(data, "Operating points by prevalence", ChartType.XY_SCATTER)
			.bind(X, t -> t.getFirst())
			.bind(Y, t -> t.getSecond().getValue())
			.withAxes("prevalence","operating point")
			.config().withXScale(0F, 1F)
			.render();
		
		
		List<Triple<Double,Double,Double>> tmpData = new ArrayList<>();
		for (Cutoff c: binned) {
			for (double prevalence = 0D; prevalence <1.01D; prevalence += 0.01D) {
				tmpData.add(Triple.create(c.getValue(), prevalence, c.deltaCost(prevalence)));
			}
		}
		
		figures.withNewChart(tmpData, "Operating point surface", ChartType.XYZ_CONTOUR)
		.bind(X, t -> t.getFirst())
		.bind(Y, t -> t.getSecond())
		.bind(Z, t -> t.getThird())
		.withAxes("prevalence","operating point")
		.config()
			.withXScale(0F, 1F)
			.withYScale(0F, 1F)
			.withCustomCommand("set view 50,20")
		.render();
	
			
		
	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);

	

}





