package uk.co.terminological.nlptools;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class StringCrossMapperTest extends TestCase {

	static final String[] strings = {
			"one two three four",
			"one two three",
			"one two three four five",
			"one two three four",
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
		mapper.getBestMatches().forEach((k,v) -> System.out.println(k.getIdentifier()+"\t"+v.getIdentifier()));
	}

}
