package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.iterators.PeekingIterator;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {

	
	
	public static void main(String[] args) throws FileNotFoundException, ParserException {
		
		// TODO Auto-generated method stub
		Path input = Paths.get(args[0]);
		
		Delimited in = Delimited.fromFile(input.toFile()).tsv().noIdentifiers().withLabels("actual","predicted","prob_pos","prob_neg").begin();
		
		EavMap<String,String,String> tmp = in.getContents();
		// EavMap<String,String,String> tmp2 = tmp.transpose();
		
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
		
		public List<Prediction> getPredictions() {
			List<Prediction> tmp = new ArrayList<>(predictions);
			tmp.sort(Comparator.comparing(Prediction::getPredicted));
			return tmp;
		}
		
		public List<Cutoff> getCutoffs(Double resolution) {
			
			PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
			Cutoff c = null;
			List<Cutoff> out = new ArrayList<>();
			
			for (Double i=resolution; i<=1; i+=resolution) {
				
				int count = 0;
				int actuals = 0;
				while (preds.hasNext() && preds.peek().getPredicted() < i) {
					count += 1;
					actuals += preds.next().getActual() ? 1 : 0;
				}
				
				c = new Cutoff(i, actuals, count);
				out.add(c);
				
			}
			
			return out;
			
		}
		
		
	}
	
	public static class Cutoff {

		Double value;
		Integer actualPositives;
		Integer predictedNegatives;
		
		public Cutoff(Double value, Integer actualPositives, Integer predictedNegatives) {
			super();
			this.value = value;
			this.actualPositives = actualPositives;
			this.predictedNegatives = predictedNegatives;
		}

		public Double getValue() {
			return value;
		}

		public void setValue(Double value) {
			this.value = value;
		}

		public Integer getActualPositives() {
			return actualPositives;
		}

		public void setActualPositives(Integer actualPositives) {
			this.actualPositives = actualPositives;
		}

		public Integer getPredictedNegatives() {
			return predictedNegatives;
		}

		public void setPredictedNegatives(Integer predictedNegatives) {
			this.predictedNegatives = predictedNegatives;
		}
		
	}
	
	/*public static class Grouping<X,Y> {
		Function<X,Y> value;
		Predicate<Y> test;
		List<X> members = new ArrayList<>();	
		 
		
	}*/
	
	
	
}





