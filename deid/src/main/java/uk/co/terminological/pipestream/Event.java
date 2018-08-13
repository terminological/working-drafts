package uk.co.terminological.pipestream;

public interface Event<Y> {

	EventMetadata<Y> getMetadata();
	Y get();

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

		@SuppressWarnings("unchecked")
		public static <Z> EventMetadata<Z> defaultFor(Z instance) {
			EventMetadata<Z> out = new EventMetadata<Z>(
					Integer.toHexString(instance.hashCode()),
					instance.getClass().getCanonicalName(),
					(Class<Z>) instance.getClass(),
					true);
			return out;
		}
		
		public static <Z> EventMetadata<Z> named(Class<Z> className, String name) {
			EventMetadata<Z> out = new EventMetadata<Z>(
					name,
					className.getCanonicalName(),
					className,
					true);
			return out;
		}
		
		public EventMetadata<Y> singleShot() {
			this.reusable = false;
			return this;
		}
	}


	public static abstract class Default<Y> implements Event<Y> {

		EventMetadata<Y> metadata;
		
		public Default() {
			
		}
		
		public Default(Y input) {
			 metadata = EventMetadata.defaultFor(input);
		}
		
		public Default(EventMetadata<Y> metadata) {
			 this.metadata = metadata;
		}
		
		public EventMetadata<Y> getMetadata() {
			return metadata;
		}

	}
}