package uk.co.terminological.pipestream.idea3;

public interface Event<Y> {

	EventMetadata<Y> getMetadata();
	Y get();

	public static class EventMetadata<Y> extends Metadata {
		
		boolean multiProcess;
		Class<Y> type;
		
		public boolean multiProcess() {
			return multiProcess;
		}
		
		public Class<Y> getType(){
			return type;
		};

		public EventMetadata(String name, String typeDescription, Class<Y> type, boolean multiProcess) {
			super(name,typeDescription); 
			 this.type = type; 
			 this.multiProcess = multiProcess;
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
	}


	public static abstract class Default<Y> implements Event<Y> {

		public EventMetadata<Y> getMetadata() {
			return EventMetadata.defaultFor(this.get());
		}

	}
}