package uk.co.terminological.pipestream;

import java.util.List;

/**
 * EventGenerators create one or more events initiated by the event bus. Their execute method is called repeatedly by the eventbus execute method
 * until returning false;
 * @author robchallen
 *
 * @param <Y> The type of output of this event generator 
 */
public interface EventGenerator<Y> extends EventBusAware {
	
	public GeneratorMetadata getMetadata();
	public void send(Event<Y> event);
	public boolean execute();
	
	public static class GeneratorMetadata extends Metadata {
		
		public GeneratorMetadata(String typeDescription) {
			super(typeDescription); 
		}

	}
	
	public static abstract class Default<Y> implements EventGenerator<Y> {
		
		public void send(Event<Y> event) {
			getEventBus().receive(event, getMetadata());
		}

		public abstract List<Event<Y>> generate();
		
		public boolean execute() {
			List<Event<Y>> tmp = generate();
			tmp.forEach(e -> {
				getEventBus().logInfo("Generating: "+e.getMetadata().toString());
				this.send(e);
			});
			return false;
		}
		
		GeneratorMetadata metadata;
		
		public Default(GeneratorMetadata metadata) {
			this.metadata = metadata;
		}
		
		public GeneratorMetadata getMetadata() {
			return metadata;
		}
		
	}
	
	public static abstract class Watcher<Y> implements EventGenerator<Y> {

		GeneratorMetadata metadata;
		boolean interrupted = false;
		Object watcher = null;
		
		public Watcher(GeneratorMetadata metadata) {
			this.metadata = metadata;
		}
		
		@Override
		public GeneratorMetadata getMetadata() {
			return metadata;
		}

		@Override
		public void send(Event<Y> event) {
			getEventBus().receive(event, getMetadata());
		}

		@Override
		public boolean execute() {
			if (watcher == null) {
				watcher = setupWatcher();
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					//continue
				}
			}
			return interrupted;
		}
		
		public abstract Object setupWatcher();
		public abstract void tearDownWatcher();
		
		public void interrupt() {
			tearDownWatcher();
			this.interrupted = true;
		}
		
	}
	
}