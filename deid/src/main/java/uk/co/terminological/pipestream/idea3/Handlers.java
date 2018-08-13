package uk.co.terminological.pipestream.idea3;

import java.util.Optional;

public class Handlers {

	
	public abstract class Adaptor<X,Y> extends EventHandler.Default<Event<X>> implements EventGenerator<Y>  {

		@Override
		public abstract boolean canHandle(Event<?> event);

		@Override
		public void handle(Event<X> event) {
			send(convert(event.get()));
		}
		
		public abstract Event<Y> convert(X input);

		@Override
		public void send(Event<Y> event) {
			getEventBus().receive(event, getMetadata());
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
