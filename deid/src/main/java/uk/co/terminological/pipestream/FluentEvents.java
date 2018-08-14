package uk.co.terminological.pipestream;

import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.Handlers.Adaptor;

public class FluentEvents {

	public static class Events {
		
		public static <Y> Event<Y> event(Y instance) {
			return namedTypedEvent(instance,null,null);
		}
		
		public static <Y> Event<Y> namedEvent(Y instance, Function<Y,String> nameMapper) {
			return namedTypedEvent(instance,nameMapper,null);
		}
		
		public static <Y> Event<Y> typedEvent(Y instance, Function<Y,String> typeMapper) {
			return namedTypedEvent(instance,null,typeMapper);
		}
		
		public static <Y> Event<Y> namedEvent(Y instance, String name) {
			return namedTypedEvent(instance,(y -> name),null);
		}
		
		public static <Y> Event<Y> typedEvent(Y instance, String type) {
			return namedTypedEvent(instance,null,(y -> type));
		}
		
		public static <Y> Event<Y> namedTypedEvent(Y instance, String name, String type) {
			return namedTypedEvent(instance,(y -> name),(y -> type));
		}
				
		@SuppressWarnings("unchecked")
		public static <Y> Event<Y> namedTypedEvent(Y instance,
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
			return matchName(name).and(matchType(type));
		}

		public static Predicate<Event<?>> matchName(String name) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return t.getMetadata().name().orElse("").equals(name);
				}
			};
		}
		
		public static Predicate<Event<?>> matchType(String type) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return t.getMetadata().typeDescription().equals(type);
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
		
		public static Predicate<Event<?>> matchMessageClass(Class<?> clazz) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return clazz.equals(t.getMetadata().getType());
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
					return Events.namedTypedEvent(
							converter.apply(input),
							nameMapper, typeMapper);
				}
			};
		}
	}
}
