package uk.co.terminological.pipestream.idea3;

public class Test {
	
	
	public interface Event {}
	
	public interface EventBus {
		
		void registerHandler(EventHandler<?> handler);
		void receive(Event message);
		void dispatchMessages();
		
	}
	
		
	public interface EventHandler<X extends Event> {
		void handle(X message);
	}
	
	
	public interface EventGenerator<X extends Event> {
		
		EventBus eventBus();
		void sendMessage(X message);
		
	}

}
