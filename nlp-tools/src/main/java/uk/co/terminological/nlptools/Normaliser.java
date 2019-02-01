package uk.co.terminological.nlptools;

import java.util.function.Function;

public interface Normaliser extends Function<String,String> {
	
	static Normaliser DEFAULT = string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^\\p{IsAlphabetic}^\\p{IsDigit}^\\s]", "-").replaceAll("-*\\s+-*", " ").toLowerCase();
	static Normaliser ASCII = string -> string.replaceAll("[_,\\.]"," ").replaceAll("[^a-zA-Z0-9\\s]", "-").replaceAll("-*\\s+-*", " ").toLowerCase();
	
}