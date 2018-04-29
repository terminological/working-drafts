package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.list.TransformedList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import com.google.common.collect.Lists;

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
		
		//tmp.stream().limit(20).forEach(System.out::println);

		tmp.streamEntities()
		.map(kv -> kv.getValue())
		.map(m -> new Prediction(
				convert01TF.apply(m.get("actual")),
				Double.parseDouble(m.get("prob_pos"))))
		.forEach(p -> res.add(p));;

		List<Cutoff> binned = res.getCutoffs(0.01D);
		// tmp.numberEntities();
		
		System.out.println(StringUtils.joinWith("\t", "getValue", "tp","fp","fn","tn","sensitivity","specificity", "smoothedSensitivity","deltaSensitivity","cumulativeProbability",
				"probabilityDensity","smoothedProbabilityDensity","probabilityDensityOverDeltaSensitivity","deltaFOverGPrime"));
		binned.stream().forEach(System.out::println);
		
		//int i=0;
		//while (binned.get(i).deltaFOverGPrime() < 0) i++;
		//Double xIntercept = (binned.get(i-1).deltaFOverGPrime()*binned.get(i-1).getValue()+binned.get(i).deltaFOverGPrime()*binned.get(i).getValue()) / 
		//		(binned.get(i-1).getValue()+binned.get(i).getValue());

		List<Double> tmp2 = Arrays.asList(0D,1D,2D,3D,4D,5D,6D);
		Iterator<List<Double>> tmp3 = SavitzkyGolay.tailed(tmp2,5);
		while (tmp3.hasNext()) {
			System.out.println(tmp3.next());
		}

		
		
		
		
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
	    
		charts.add(QuickChart.getChart("E", "cutoff", "first derivative sensitivity","g'(x)", 
				Lists.transform(binned, c->c.getValue()),
				Lists.transform(binned, c->c.deltaSensitivity())));
	    
		List<Cutoff> filtered = binned.stream().filter(c -> c.probabilityDensityOverDeltaSensitivity() < 10 && c.probabilityDensityOverDeltaSensitivity() > -10).collect(Collectors.toList());
		
		charts.add(QuickChart.getChart("F", "cutoff", "f(x)/g'(x)","f(x)/g'(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.smoothedFOverGPrime())));
	    
		filtered = binned.stream().filter(c -> c.deltaFOverGPrime() < 10 && c.deltaFOverGPrime() > -10).collect(Collectors.toList());
		
		charts.add(QuickChart.getChart("F", "cutoff", "f(x)/g'(x)","deriv f(x)/g'(x)", 
				Lists.transform(filtered, c->c.getValue()),
				Lists.transform(filtered, c->c.deltaFOverGPrime())
				));
			
	    
		
	    new SwingWrapper<XYChart>(charts).displayChartMatrix();
		
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

			for (Double i=0D; i<1+resolution*0.95; i+=resolution) {

				while (preds.hasNext() && preds.peek().getPredicted() < i) {
					predNeg += 1;
					falseNeg += preds.next().getActual() ? 1 : 0;
				}

				c = new Cutoff(i, falseNeg, predNeg, totalPositive(), total(), out, out.size(), resolution);
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
		Double resolution;

		public Cutoff(Double value, Integer falseNegatives, Integer predictedNegatives, Integer totalPositives, Integer total, List<Cutoff> all, int index, Double resolution) {
			super();
			this.value = value;
			this.falseNegatives = falseNegatives;
			this.predictedNegatives = predictedNegatives;
			this.total = total;
			this.totalPositives = totalPositives;
			this.all = all;
			this.index = index;
			this.resolution = resolution;
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

		public Double smoothedSensitivity() {
			return SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_7_cubic(), false, index, c -> c.sensitivity()); 
		}
		
		Double deltaS = null;
		public Double deltaSensitivity() {
			if (deltaS == null) deltaS = 
					SavitzkyGolay.convolute(all, SavitzkyGolay.derivative_7_quartic(resolution), false, index, c -> c.smoothedSensitivity());
			return deltaS;
		}

		public Double cumulativeProbability() {
			return ((double) predictedNegatives)/total;
		}
		
		public Double probabilityDensity() {
			if (index == 0) return cumulativeProbability();
			return ((double) (predictedNegatives-all.get(index -1).predictedNegatives))/total/resolution;
		}
		
		Double smoothPD = null;
		public Double smoothedProbabilityDensity() {
			if (smoothPD == null) smoothPD = 
					SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_7_cubic(), false, index, c -> c.probabilityDensity());
			return smoothPD;
		}
		
		public Double probabilityDensityOverDeltaSensitivity() {
			return probabilityDensity()/deltaSensitivity(); 
		}
		
		public Double smoothedFOverGPrime() {
			return SavitzkyGolay.convolute(all, SavitzkyGolay.smooth_N_sliding(13), false, index, c -> c.probabilityDensityOverDeltaSensitivity());
		}
		
		public Double deltaFOverGPrime() {
			return SavitzkyGolay.convolute(all, SavitzkyGolay.derivative_7_quartic(resolution), false, index, c -> c.probabilityDensityOverDeltaSensitivity()); 
		}
		
		public String toString() {
			//return StringUtils.joinWith("\t", this.value, this.falseNegatives, this.predictedNegatives);
			return StringUtils.joinWith("\t", getValue(), tp(),fp(),fn(),tn(),sensitivity(),specificity(), smoothedSensitivity(),deltaSensitivity(),cumulativeProbability(),
					probabilityDensity(),smoothedProbabilityDensity(),probabilityDensityOverDeltaSensitivity(),deltaFOverGPrime());
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

		static double[] smooth_5_cubic() {return new double[]{-3D/35,12D/35,17D/35,12D/35,-3D/35};}
		
		static double[] smooth_7_cubic() {return new double[]{-2D/21,3D/21,6D/21,7D/21,6D/21,3D/21,-2D/21};}
		static double[] smooth_9_cubic() {return new double[]{-21D/231,14D/231,39D/231,54D/231,59D/231,54D/231,39D/231,14D/231,-21D/231};}

		static double[] derivative_5_quad(double w) {return new double[]{-2D/(10*w),-1D/(10*w),0,1D/(10*w),2D/(10*w)};}
		static double[] derivative_7_quad(double w) {return new double[]{-3D/(28*w),-2D/(28*w),-1D/(28*w),0,1D/(28*w),2D/(28*w),3D/(10*w)};}

		static double[] derivative_5_quartic(double w) {return new double[]{1D/(12*w),-8D/(12*w),0,8D/(12*w),-1D/(12*w)};}
		static double[] derivative_7_quartic(double w) {return new double[]{22D/(252*w),-67D/(252*w),-58D/(252*w),0,58D/(252*w),67D/(252*w),-22D/(252*w)};}

		static double[] smooth_N_sliding(int n) { 
			double[] tmp = new double[n];
			Arrays.fill(tmp, 1D/n);
			return tmp;}
		
		static <X> Double convolute(List<X> input, double[] filter, boolean circular, int position, Function<X,Double> toDouble) {
			List<X> window = circular ? circular(input, filter.length, position) : tailed(input, filter.length, position);
			Double collect = 0D;
			for (int i=0; i<filter.length; i=i+1) {
				collect += toDouble.apply(window.get(i))*filter[i];
				//TODO: deal with null etc
			}
			return collect;
		}
		
		static Double convolute(List<Double> input, double[] filter, boolean circular, int position) {
			List<Double> window = circular ? circular(input, filter.length, position) : tailed(input, filter.length, position);
			Double collect = 0D;
			for (int i=0; i<filter.length; i++) {
				collect += window.get(i)*filter[i];
				//TODO: deal with null etc
			}
			return collect;
		}
		
		static List<Double> convolute(List<Double> input, double[] filter, boolean circular) {
			int size = filter.length;
			List<Double> out = new ArrayList<>();
			Iterator<List<Double>> window = circular ? circular(input, size) : tailed(input, size);
			while (window.hasNext()) {
				List<Double> tmp = window.next();
				Double collect = 0D;
				for (int i=0; i<filter.length; i++) {
					collect += tmp.get(i)*filter[i];
					//TODO: deal with null etc
				}
				out.add(collect);
			}
			return out;
		}

		//TODO refactor as stand alone windowing function
		static <X> Iterator<List<X>> circular(final List<X> input, int width) { 

			if (input.size() < width) throw new ArrayIndexOutOfBoundsException("window width larger than list size");
			return new Iterator<List<X>>() {

				int position = 0;

				@Override
				public boolean hasNext() {
					return position < input.size();
				}

				@Override
				public List<X> next() {
					return circular(input,width,position++);
				}

			};
		}
		
		static <X> List<X> circular(final List<X> input, int width, int position) {
			if (position > input.size() || position < 0) throw new ArrayIndexOutOfBoundsException("window position outside of list");
			int i = width/2;
			int start = position-i;
			int end = position+i+1;
			List<X> out = new ArrayList<>();
			if (start < 0) {
				out.addAll(input.subList(input.size()+start, input.size()));
				out.addAll(input.subList(0, end));
			} else if (end > input.size()) {
				out.addAll(input.subList(start, input.size()));
				out.addAll(input.subList(0, end-input.size()));
			} else {
				out.addAll(input.subList(start, end));
			}
			return out;
		}

		static <X> Iterator<List<X>> symmetric(final List<X> input, int width) { 
			if (input.size() < width) throw new ArrayIndexOutOfBoundsException("window width larger than list size");
			return new Iterator<List<X>>() {

				int position = 0;
				

				@Override
				public boolean hasNext() {
					return position < input.size();
				}

				@Override
				public List<X> next() {
					if (!hasNext()) throw new NoSuchElementException();
					return symmetric(input,width,position++);
				}

			};
		}
		
		static <X> List<X> symmetric(final List<X> input, int width, int position) {
			if (position > input.size() || position < 0) throw new ArrayIndexOutOfBoundsException("window position outside of list");
			int i = width/2;
			int start = position-i;
			int end = position+i+1;
			List<X> out = new ArrayList<>();
			if (start < 0) {
				out.addAll(input.subList(1, 1-start));
				Collections.reverse(out);
				out.addAll(input.subList(0, end));
			} else if (end > input.size()) {
				List<X> tmp = input.subList(2*input.size()-end-1, input.size()-1);
				Collections.reverse(tmp);
				out.addAll(input.subList(start, input.size()));
				out.addAll(tmp);
			} else {
				out.addAll(input.subList(start, end));
			}
			start = start == input.size() ? 0 : start+1;
			end = end == input.size() ? 0 : end+1;
			return out;
		}

		public static <X> Iterator<List<X>> tailed(final List<X> input, int width) {
			if (input.size() < width) throw new ArrayIndexOutOfBoundsException("window width larger than list size");
			return new Iterator<List<X>>() {

				int position = 0;
				

				@Override
				public boolean hasNext() {
					return position < input.size();
				}

				@Override
				public List<X> next() {
					if (!hasNext()) throw new NoSuchElementException();
					return tailed(input,width,position++);
				}

			};
		}
		
		static <X> List<X> tailed(final List<X> input, int width, int position) {
			if (position > input.size() || position < 0) throw new ArrayIndexOutOfBoundsException("window position outside of list");
			int i = width/2;
			int start = position-i;
			int end = position+i+1;
			List<X> out = new ArrayList<>();
			int tmp = start;
			while (tmp<0) {
				out.add(input.get(0)); 
				tmp++;
			}
			out.addAll(input.subList(tmp, end>input.size()-1 ? input.size()-1 : end));
			tmp = end;
			while (tmp > input.size()-1) {
				out.add(input.get(input.size()-1));
				tmp-=1;
			}
			start = start == input.size() ? 0 : start+1;
			end = end == input.size() ? 0 : end+1;
			return out;
		}

	}

}





