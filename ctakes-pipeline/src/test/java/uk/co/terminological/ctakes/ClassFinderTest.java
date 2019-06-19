package uk.co.terminological.ctakes;

import org.junit.Test;

import uk.co.terminological.ctakes.ClassFinder.Visitor;

public class ClassFinderTest {

	@Test
	public void testClassFinder() {
		ClassFinder.findClasses(new Visitor<String>() {
		    @Override
		    public boolean visit(String clazz) {
		        System.out.println(clazz);
		        return true; // return false if you don't want to see any more classes
		    }
		});
	}
	
	
	@Test
	public void testResourceFinder() {
		ClassFinder.findResources(new Visitor<String>() {
		    @Override
		    public boolean visit(String clazz) {
		        if(clazz.startsWith("org/apache/ctakes")) 
		        	System.out.println(clazz);
		        return true; // return false if you don't want to see any more classes
		    }
		});
	}
}
