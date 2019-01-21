package uk.co.terminological.nlptools;

import java.util.List;
import java.util.Optional;

import uk.co.terminological.datatypes.FluentList;

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
		this.previous.ifPresent( p -> p.next = Optional.of(this) );
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
			if (n==1) {
				return FluentList.create(next.get());
			} else {
				List<TermInstance> tmp = getNext(n-1);
				if (tmp.size() == 0) {
					tmp.add(next.get()); 
				} else {
					tmp.set(0, next.get());
				}
				return tmp;
			}
		} else {
			return FluentList.empty();
		}
	}
	
	public List<TermInstance> getPrevious(int n) {
		if (previous.isPresent()) {
			if (n==1) {
				return FluentList.create(previous.get());
			} else {
				List<TermInstance> tmp = getPrevious(n-1);
				if (tmp.size() == 0) {
					tmp.add(previous.get()); 
				} else {
					tmp.set(0, previous.get());
				}
				return tmp;
			}
		} else {
			return FluentList.empty();
		}
	}
	
	
}
