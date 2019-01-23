package uk.co.terminological.nlptools;

import java.util.function.Function;
import java.util.stream.Stream;

interface Tokeniser extends Function<String,Stream<String>> {}