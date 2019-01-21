package uk.co.terminological.nlptools;

import java.util.Optional;

public class TermInstance {

	Optional<Term> previous = Optional.empty();
	Term current;
	Optional<Term> next = Optional.empty();
	
	public TermInstance(Term current) {
		this.current = current;
	}
	
	public TermInstance(Term current, Term previous) {
		this(current);
		this.previous = Optional.ofNullable(previous);
	}
	
	public Term getTerm() {
		return current;
	}
}
