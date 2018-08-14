package uk.co.terminological.pipestream;

import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.Handlers.Adaptor;

public class FluentEvents {

	public static class Metadata {
		
		@SuppressWarnings("unchecked")
		public static <Y> EventMetadata<Y> forEvent(Y instance,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper) {
			if (nameMapper == null) nameMapper = (i -> Integer.toHexString(i.hashCode()));
			if (typeMapper == null) typeMapper = (i -> i.getClass().getCanonicalName());
			return new EventMetadata<Y>(
					nameMapper.apply(instance),
					typeMapper.apply(instance),
					(Class<Y>) instance.getClass(),
					true);
		}
		
		
	}
	
	public static class Events {
		
		public static <Y> Event<Y> event(Y instance) {
			return namedTypedEvent(instance, (Function<Y,String>) null,(Function<Y,String>) null);
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
				
		public static <Y> Event<Y> namedTypedEvent(Y instance,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper
				) {
			return new Event.Default<Y>(
					Metadata.forEvent(instance, nameMapper, typeMapper),
					instance);
		}
	}

	public static class Predicates {
		
		public static Predicate<Event<?>> matchNameAndType(String name, String type) {
			return matchName(name).and(matchType(type));
		}

		public static Predicate<Event<?>> matchName(String name) {
			return matchName(s -> s.equals(name));
		}
		
		public static Predicate<Event<?>> matchType(String type) {
			return matchType(s -> s.equals(type));
		}

		public static Predicate<Event<?>> matchNameAndType(Predicate<String> name, Predicate<String> type) {
			return matchName(name).and(matchType(type));
		}

		public static Predicate<Event<?>> matchName(Predicate<String> name) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return name.test(t.getMetadata().name().orElse(""));
				}
			};
		}
		
		public static Predicate<Event<?>> matchType(Predicate<String> type) {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return type.test(t.getMetadata().typeDescription());
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
