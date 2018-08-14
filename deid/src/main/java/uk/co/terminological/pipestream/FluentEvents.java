package uk.co.terminological.pipestream;

import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.Handlers.Adaptor;

public class FluentEvents {

	
	@SuppressWarnings("unchecked")
	public static <Y> Event<Y> event(Y instance,
			Function<Y,String> nameMapper) {
		return new Event.Default<Y>(
				(EventMetadata<Y>) EventMetadata.named(
						instance.getClass(),
						nameMapper.apply(instance)),instance);
	}
	
	public static <X,Y> Adaptor<X,Y> adaptor(
			Class<X> input, 
			Class<Y> output, 
			Predicate<Event<?>> acceptEvent,
			Function<X,Y> converter,
			Function<Y,String> nameForResult
			
			) {
		return new Adaptor<X,Y>() {

			@Override
			public boolean canHandle(Event<?> event) {
				return acceptEvent.test(event);
			}

			@Override
			public Event<Y> convert(X input) {
				return event(
						converter.apply(input),
								nameForResult);
			}
			
		};
	}
	
}
