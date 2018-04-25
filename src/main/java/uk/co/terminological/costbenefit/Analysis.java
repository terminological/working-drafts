package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {

	
	
	public static void main(String[] args) throws FileNotFoundException, ParserException {
		
		// TODO Auto-generated method stub
		Path input = Paths.get(args[0]);
		
		Delimited in = Delimited.fromFile(input.toFile()).tsv().noIdentifiers().withLabels("actual","predicted","prob_pos","prob_neg").begin();
		
		EavMap<String,String,String> tmp = in.getContents();
		EavMap<String,String,String> tmp2 = tmp.transpose();
		
		ClassifierResult res = new ClassifierResult();
		
		tmp.streamEntities()
			.map(kv -> kv.getValue())
			.map(m -> new Prediction(
				convert01TF.apply(m.get("actual")),
				Double.parseDouble(m.get("prediction"))))
			.forEach(p -> res.add(p));;
		
		// tmp.numberEntities();
		
		
		

	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);
	
	public static class Prediction {
		Prediction(Boolean actual, Double predicted) {
			this.actual = actual;
			this.predicted = predicted;
		}
		
		Boolean actual;
		Double predicted;
		
		public Double getPredicted() {return predicted;}
		public Boolean getActual() {return actual;}
		
	}
	
	public static class ClassifierResult {
		
		List<Prediction> predictions = new ArrayList<>();
		
		public void add(Prediction p) {this.predictions.add(p);}
		
		public Stream<Prediction> streamByPrediction() {
			return predictions.stream().sorted(Comparator.comparing(Prediction::getPredicted));
		}
		
		
		
	}
	
	public static class BandedCutoff {
		
	}
	
	/*public static class Grouping<X,Y> {
		Function<X,Y> value;
		Predicate<Y> test;
		List<X> members = new ArrayList<>();	
		 
		
	}*/
	
	
	
}




