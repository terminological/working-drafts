package uk.co.terminological.costbenefit;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.tabular.Delimited;

public class Analysis {

	
	
	public static void main(String[] args) throws FileNotFoundException, ParserException {
		
		// TODO Auto-generated method stub
		Path input = Paths.get(args[0]);
		
		Delimited in = Delimited.fromFile(input.toFile()).tsv().noIdentifiers().withLabels("actual","predicted","prob_pos","prob_neg").begin();
		
		EavMap<Long,Integer,String> tmp = in.getContentsByRow();
		tmp.numberEntities();
		tmp.stream().forEach(System.out::println);

	}

}
