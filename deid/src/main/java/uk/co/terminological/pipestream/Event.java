package uk.co.terminological.pipestream;

import java.util.HashMap;
import java.util.Map;

public interface Event<Y> {

	EventMetadata<Y> getMetadata();
	Y get();
	Event<Y> put(String key, Object o);
	Object get(String key);

	public static class EventMetadata<Y> extends Metadata {
		
		boolean reusable;
		Class<Y> type;
		
		public boolean reusable() {
			return reusable;
		}
		
		public Class<Y> getType(){
			return type;
		};

		public EventMetadata(String name, String typeDescription, Class<Y> type, boolean multiProcess) {
			super(name,typeDescription); 
			 this.type = type; 
			 this.reusable = multiProcess;
		}

		public EventMetadata<Y> singleShot() {
			this.reusable = false;
			return this;
		}
	}

	//TODO: Some extension of event or eventMetadata which includes a version date

	public static class Default<Y> implements Event<Y> {

		EventMetadata<Y> metadata;
		Y message;
		Map<String, Object> store = new HashMap<>();
		
		@Deprecated
		public Default(Y input) {
			 this(FluentEvents.Metadata.forEvent(input),input);
		}
		
		public Default(EventMetadata<Y> metadata,Y input) {
			 this.metadata = metadata;
			 this.message = input;
		}
		
		public EventMetadata<Y> getMetadata() {
			return metadata;
		}

		public Y get() {return message;}

		@Override
		public Event<Y> put(String key, Object value) {
			store.put(key, value);
			return this;
		}

		@Override
		public Object get(String key) {
			return store.get(key);
		}
		
	}
}