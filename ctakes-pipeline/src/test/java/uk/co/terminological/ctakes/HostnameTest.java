package uk.co.terminological.ctakes;

import java.lang.management.ManagementFactory;

import org.junit.Test;



public class HostnameTest {

	@Test
	public void test() {
		
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());
	}

}
