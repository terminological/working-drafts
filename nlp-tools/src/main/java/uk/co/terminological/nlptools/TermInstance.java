package uk.co.terminological.nlptools;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TermInstance {

	private Optional<TermInstance> previous = Optional.empty();
	private Term current;
	private Optional<TermInstance> next = Optional.empty();
	
	public TermInstance(Term current) {
		this.current = current;
	}
	
	public TermInstance(Term current, TermInstance previous) {
		this(current);
		this.previous = Optional.ofNullable(previous);
	}
	
	public Term getTerm() {
		return current;
	}
	
	public boolean isFirst() {
		return !previous.isPresent();
	}
	
	public boolean isLast() {
		return !next.isPresent();
	}
	
	public List<TermInstance> getNext(int n) {
		if (next.isPresent()) {
			if (n==1) return Collections.singletonList(next.get());
			return next.
		}
	}
}
