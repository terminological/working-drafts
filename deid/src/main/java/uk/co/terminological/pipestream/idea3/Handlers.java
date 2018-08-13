package uk.co.terminological.pipestream.idea3;

public class Handlers {

	
	public interface Adaptor<X extends Event<?>,Y> extends EventHandler<X>, EventGenerator<Y> {
		
	}
	
	
	public class Collector implements EventHandler<Event<?>> {

		@Override
		public boolean canHandle(Event<?> event) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void handle(Event<?> event) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
