package uk.co.terminological.pipestream;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.pipestream.EventBusTest.TestGenerator;

public class FluentEventBusTest {
			EventBus bus;
		
		public static void main(String[] args) throws Exception {
			EventBusTest tmp = new EventBusTest();
			tmp.setUp();
			tmp.test();
		}
		
		@Before
		public void setUp() throws Exception {

			BasicConfigurator.configure();
			bus = EventBus.get();
					
		}

		@Test
		public final void test() {
			EventBus.get().logInfo("Starting");
			TestGenerator stringLoader = new TestGenerator(); 
			
			while(stringLoader.execute());
			
			EventBus.get().logInfo("Finished");
		}
}
