package uk.co.terminological.nlptools;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class StringCrossMapperTest extends TestCase {

	static final String[] strings = {
			"one",
			"one two",
			"one two three",
			"two three four",
			"three four five",
			"four five",
			"five"
	};
	
	
	StringCrossMapper mapper;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.mapper = new StringCrossMapper();
		for (int i=0; i<strings.length; i++) {
			this.mapper.addSource("S_"+i, strings[i]);
			this.mapper.addTarget("T_"+i, strings[i]);
		}
	}
	
	@Test
	public void testTfIDF() {
		mapper.getAllMatchesBySimilarity(0D, d -> d.termsByTfIdf()).forEach(
				(k,v) -> v.forEach(
						(k2,v2) -> System.out.println(k.getIdentifier()+"\t"+k2.getIdentifier()+"\t"+v2.toString())
					)
			);
		
		mapper.getSource().getTermsByTotalEntropy().forEach(term -> {
			System.out.println(term.toString() + "\t" + mapper.getSource().totalShannonEntropy(term));
		});
	}

}
