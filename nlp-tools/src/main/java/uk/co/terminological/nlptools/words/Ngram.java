package uk.co.terminological.nlptools.words;

public class Ngram {

	int index;
	
	
	// Only construct via dictionary
	protected Ngram(int int1) {
		index = int1;
	}

	@Deprecated
	public String getValue(NGramDictionary dict) {
		return dict.lookup(index);
	}

	@Override
	public int hashCode() {
		return index;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ngram other = (Ngram) obj;
		if (index != other.index)
			return false;
		return true;
	}

}
