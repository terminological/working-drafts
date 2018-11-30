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
	protected void setUp() throws Exception {
		super.setUp();
		this.mapper = new StringCrossMapper();
		for (int i=0; i<strings.length; i++) {
			this.mapper.addSource("S_"+i, strings[i]);
			this.mapper.addSource("T_"+i, strings[i]);
		}
	}
	
	@Test
	protected void testTfIDF() {
		mapper.getBestMatches().forEach((k,v) -> System.out.println(k.getIdentifier()+"\t"+v.getIdentifier());;
	}

}
