package uk.co.terminological.nlptools;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Filters {

	
	
	
	public static Predicate<String> stopwords(List<String> stopwords, Normaliser normaliser, Tokeniser tokeniser) {
		Set<String> stopWordList = stopwords.stream()
				.map(normaliser)
				.flatMap(tokeniser)
				.collect(Collectors.toSet());
		return t -> !stopWordList.contains(t);
	}
	
	public static Predicate<String> shorterThan(int size) {
		return t -> t.length() < size;
	}
	
	public static Predicate<String> number() {
		return t -> t.matches("[\\-+]?[0-9]*[\\.]*[0-9]+");
	}
	
}
