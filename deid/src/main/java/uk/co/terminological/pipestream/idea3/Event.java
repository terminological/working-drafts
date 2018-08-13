package uk.co.terminological.pipestream.idea3;

public interface Event<Y> {
	
	Metadata<Y> getMetadata();
	Y get();
	
	
	
	public static abstract class Default<Y> implements Event<Y> {

		public Metadata<Y> getMetadata() {
			return Metadata.defaultFor(this.get());
		}

	}
}