package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		
		List<Boolean> actual = tmp2.get("actual").values().stream().map(convert01TF).collect(Collectors.toList());
		List<Boolean> predicted = tmp2.get("predicted").values().stream().map(convert01TF).collect(Collectors.toList());
		List<Double> prob_pos = tmp2.get("prob_pos").values().stream().map(convertToDouble).collect(Collectors.toList());
		List<Double> prob_neg = tmp2.get("prob_neg").values().stream().map(convertToDouble).collect(Collectors.toList());
		
		// tmp.numberEntities();
		actual.stream().limit(50).forEach(System.out::println);
		
		

	}

	static Function<String,Boolean> convert01TF = s -> s.equals("1") ? true : (s.equals("0") ? false: null);
	static Function<String,Double> convertToDouble = s -> Double.parseDouble(s);
	
}




