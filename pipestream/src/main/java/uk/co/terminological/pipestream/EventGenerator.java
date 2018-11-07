package uk.co.terminological.pipestream;

import java.util.List;

public interface EventGenerator<Y> extends EventBusAware {
	
	public Metadata getMetadata();
	void send(Event<Y> event);
	public boolean execute();
	
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
		
		Metadata metadata;
		
		public Default(Metadata metadata) {
			this.metadata = metadata;
		}
		
		public Metadata getMetadata() {
			return metadata;
		}
		
	}
	
	public static abstract class Watcher<Y> implements EventGenerator<Y> {

		Metadata metadata;
		boolean interrupted = false;
		Object watcher = null;
		
		public Watcher(Metadata metadata) {
			this.metadata = metadata;
		}
		
		@Override
		public Metadata getMetadata() {
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