package uk.co.terminological.pipestream.idea3;

public class Handlers {

	
	public class Adaptor<X,Y> extends EventGenerator.Default<Y> implements EventHandler<Event<X>>  {

		@Override
		public boolean canHandle(Event<?> event) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void handle(Event<X> event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public HandlerMetadata getMetadata() {
			// TODO Auto-generated method stub
			return null;
		}
		
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

		@Override
		public HandlerMetadata getMetadata() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/*
	public interface EventIntegrator extends EventHandler<Event<?>> {
		public String eventName();
	}
	
	public interface EventIntegratorGenerator extends EventHandlerGenerator<Event<?>> {
		public String eventName();
	}
	*/
	
}
