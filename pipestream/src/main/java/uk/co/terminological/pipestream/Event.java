package uk.co.terminological.pipestream;

import java.util.HashMap;
import java.util.Map;

/**
 * Events are passive data carriers that are controlled by the event bus. 
 * @author robchallen
 *
 * @param <Y>
 */
public interface Event<Y> {

	/**
	 * A set of metadata for the message. At minimum this will contain a type description, and define the type of the 
	 * @return
	 */
	EventMetadata<Y> getMetadata();
	
	/**
	 * The main accessor for the data of the message.
	 * @return
	 */
	Y get();
	
	/**
	 * A key value store for additional data fields that need to accompany the message
	 * @param key
	 * @param o
	 * @return
	 */
	Event<Y> put(String key, Object o);
	
	/**
	 * Accessor for the 
	 * @param key
	 * @return
	 */
	Object get(String key);

	/**
	 * The event metadata class provides all non data fields on an event. 
	 * @author robchallen
	 *
	 * @param <Y>
	 */
	public static class EventMetadata<Y> extends Metadata {
		
		boolean reusable;
		Class<Y> type;
		
		/**
		 * Defines whether the event can be handled by more than one processor.
		 * Typically this will be the case unless the event is consumed - for example a stream
		 * @return
		 */
		public boolean reusable() {
			return reusable;
		}
		
		/**  
		 * The class of the data item held in the event
		 * @return
		 */
		public Class<Y> getType(){
			return type;
		};

		/**
		 * The event is usually built by specific event builders as described in {@link uk.co.terminological.pipestream.FluentEvents.Events} 
		 * but could be manually constructed if needs be.
		 * @param name - The name of the event is designed to loosely identify the instance of the event and may include
		 * @param typeDescription - A test description of the type of the event 
		 * @param type - the return class of the event
		 * @param multiProcess - is the event reusable
		 */
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

	/**
	 * A special system level event to signal shutdown of the event bus 
	 * @author robchallen
	 *
	 */
	public static class Shutdown implements Event<Void> {

		@Override
		public EventMetadata<Void> getMetadata() {
			return FluentEvents.Metadata.forEvent(Void.class, "SHUTDOWN_WARNING", "SHUTDOWN_WARNING");
		}

		@Override
		public Void get() {
			return null;
		}

		@Override
		public Event<Void> put(String key, Object o) {
			return this;
		}

		@Override
		public Object get(String key) {
			return null;
		}
		
	}
	
	//TODO: Some extension of event or eventMetadata which includes a version date

	/**
	 * The default implementation of the {@link Event} class holding a simple java object
	 * @author robchallen
	 *
	 * @param <Y>
	 */
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