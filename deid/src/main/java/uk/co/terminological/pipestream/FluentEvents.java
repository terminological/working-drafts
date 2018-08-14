package uk.co.terminological.pipestream;

import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.Handlers.Adaptor;

public class FluentEvents {

	public static class Events {
		
		public static <Y> Event<Y> event(Y instance) {
			return event(instance,null,null);
		}
		
		public static <Y> Event<Y> event(Y instance, Function<Y,String> nameMapper) {
			return event(instance,nameMapper,null);
		}
		
		public static <Y> Event<Y> namedEvent(Y instance, String name) {
			return event(instance,(y -> name),null);
		}
		
		public static <Y> Event<Y> typedEvent(Y instance, String type) {
			return event(instance,null,(y -> type));
		}
		
		public static <Y> Event<Y> namedTypedEvent(Y instance, String name, String type) {
			return event(instance,(y -> name),(y -> type));
		}
				
		@SuppressWarnings("unchecked")
		public static <Y> Event<Y> event(Y instance,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper
				) {
			if (nameMapper == null) nameMapper = (i -> Integer.toHexString(i.hashCode()));
			if (typeMapper == null) typeMapper = (i -> i.getClass().getCanonicalName());
			return new Event.Default<Y>(
					new EventMetadata<Y>(
							nameMapper.apply(instance),
							typeMapper.apply(instance),
							(Class<Y>) instance.getClass(),
							true),
					instance);
		}
	}

	public static class Predicates {
		public static Predicate<Event<?>> matchNameAndType(String name, String type) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return t.getMetadata().name().orElse("").equals(name) &&
							t.getMetadata().typeDescription().equals(type);
				}
			};
		}


		public static Predicate<Event<?>> matchEventClass(Class<? extends Event<?>> clazz) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return clazz.equals(t.getClass());
				}
			};
		}
	}

	public static class Handlers {
		public static <X,Y> Adaptor<X,Y> adaptor(
				Predicate<Event<?>> acceptEvent,
				Function<X,Y> converter,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper
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
							nameMapper, typeMapper);
				}
			};
		}
	}
}
