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
		int totalPositive = 0;
		int total = 0;
		
		public void add(Prediction p) {
			this.predictions.add(p);
			total +=1;
			totalPositive += p.getActual() ? 1:0;
		}
		
		public List<Prediction> getPredictions() {
			List<Prediction> tmp = new ArrayList<>(predictions);
			tmp.sort(Comparator.comparing(Prediction::getPredicted));
			return tmp;
		}
		
		public int totalPositive() {
			return totalPositive;
		}
		
		public int total() {
			return total;
		}
		
		public List<Cutoff> getCutoffs(Double resolution) {
			
			PeekingIterator<Prediction> preds = new PeekingIterator<>(getPredictions().iterator()); 
			Cutoff c = null;
			List<Cutoff> out = new ArrayList<>();
			
			int predNeg = 0;
			int falseNeg = 0;
			
			for (Double i=resolution; i<=1; i+=resolution) {
				
				while (preds.hasNext() && preds.peek().getPredicted() < i) {
					predNeg += 1;
					falseNeg += preds.next().getActual() ? 1 : 0;
				}
				
				c = new Cutoff(i, falseNeg, predNeg, totalPositive(), total(), out, out.size());
				out.add(c);
				
			}
			
			return out;
			
		}
		
		
	}
	
	public static class Cutoff {

		Double value;
		Integer falseNegatives;
		Integer predictedNegatives;
		Integer totalPositives;
		Integer total;
		List<Cutoff> all;
		int index;
		
		public Cutoff(Double value, Integer falseNegatives, Integer predictedNegatives, Integer totalPositives, Integer total, List<Cutoff> all, int index) {
			super();
			this.value = value;
			this.falseNegatives = falseNegatives;
			this.predictedNegatives = predictedNegatives;
			this.total = total;
			this.totalPositives = totalPositives;
			this.all = all;
			this.index = index;
		}

		public Double getValue() {
			return value;
		}

		public Integer fn() {
			return falseNegatives;
		}
		
		public Integer tn() {
			return predictedNegatives-falseNegatives;
		}
		
		public Integer tp() {
			return totalPositives-falseNegatives;
		}
		
		public Integer fp() {
			return (total-predictedNegatives)-tp();
		}
		
		public Double sensitivity() {
			return ((double) tp())/totalPositives;
		}
		
		public Double specificity() {
			return ((double) tn())/(total-totalPositives);
		}
		
		
	}
	
	/*public static class Grouping<X,Y> {
		Function<X,Y> value;
		Predicate<Y> test;
		List<X> members = new ArrayList<>();	
		 
		
	}*/
	
	public static <X,Y> Double smooth(List<X> list, int index, int width, Function<X,Double> selector) {
		width = width/2+1;
		int start = index<width ? 0 : index-width;
		int end = (list.size()-index)<width ? list.size() : index+width;
		return list.subList(start, end).stream().map(selector).collect(Collectors.averagingDouble(c->c));
	}
	
	public static class SavitzkyGolay {
		
		//https://en.wikipedia.org/wiki/Savitzky%E2%80%93Golay_filter#Appendix
		
		static double[] smooth_5_cubic() {return new double[]{-3/35,12/35,17/35,12/35,-3/35};}
		static double[] smooth_7_cubic() {return new double[]{-2/21,3/21,6/21,7/21,6/21,3/21,-2/21};}
		static double[] smooth_9_cubic() {return new double[]{-21/231,14/231,39/231,54/231,59/231,54/231,39/231,14/231,-21/231};}
		
		static double[] derivative_5_quad(double w) {return new double[]{-2/(10*w),-1/(10*w),0,1/(10*w),2/(10*w)};}
		static double[] derivative_7_quad(double w) {return new double[]{-3/(28*w),-2/(28*w),-1/(28*w),0,1/(28*w),2/(28*w),3/(10*w)};}
		
		static double[] derivative_5_quartic(double w) {return new double[]{1/(12*w),-8/(12*w),0,8/(12*w),-1/(12*w)};}
		static double[] derivative_7_quartic(double w) {return new double[]{22/(252*w),-67/(252*w),-58/(252*w),0,58/(252*w),67/(252*w),-22/(252*w)};}
		
		static List<Double> convolute(List<Double> input, double[] filter) {
			int size = filter.length;
			List<>
		}
		
	}
	
}





