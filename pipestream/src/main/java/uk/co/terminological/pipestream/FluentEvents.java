package uk.co.terminological.pipestream;

import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import uk.co.terminological.pipestream.Event.EventMetadata;
import uk.co.terminological.pipestream.EventHandler.HandlerMetadata;
import uk.co.terminological.pipestream.FileUtils.DirectoryScanner;
import uk.co.terminological.pipestream.FileUtils.FileWriter;
import uk.co.terminological.pipestream.HandlerTypes.Adaptor;
import uk.co.terminological.pipestream.HandlerTypes.EventProcessor;
import uk.co.terminological.pipestream.HandlerTypes.Processor;
import uk.co.terminological.pipestream.HandlerTypes.Terminal;

public class FluentEvents {

	public static class Metadata {
		
		private static <Y> Function<Y,String> defaultNameMapper() {
			return (i -> Integer.toHexString(i.hashCode()));
		}
		private static <Y> Function<Y,String> defaultTypeMapper() {
			return (i -> i.getClass().getCanonicalName());
		}
		
		@SuppressWarnings("unchecked")
		public static <Y> EventMetadata<Y> forEvent(Y instance,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper) {
			if (nameMapper == null) nameMapper = defaultNameMapper();
			if (typeMapper == null || typeMapper.apply(instance) == null) {
					typeMapper = defaultTypeMapper();
			}
			return new EventMetadata<Y>(
					nameMapper.apply(instance),
					typeMapper.apply(instance),
					(Class<Y>) instance.getClass(),
					true);
		}
		
		public static <Y> EventMetadata<Y> forEvent(Y instance) {
			return forEvent(instance, (Function<Y,String>) null, (Function<Y,String>) null);
		}
		
		
		public static <Y> EventMetadata<Y> forEvent(Y instance,
				Function<Y,String> nameMapper,
				String type) {
			return forEvent(instance,nameMapper,(s -> type));
		}
		
		public static <Y> EventMetadata<Y> forEvent(Y instance,
				String name,
				Function<Y,String> typeMapper) {
			return forEvent(instance,(s -> name),typeMapper);
		}
		
		public static <Y> EventMetadata<Y> forEvent(Y instance,
				String name,
				String type) {
			Function<Y,String> typeMapper = (type == null ? defaultTypeMapper() : (s -> type));
			return forEvent(instance,((Function<Y,String>) s -> name),typeMapper);
		}
		
		public static <Y> EventMetadata<Y> forEvent(Class<Y> clazz,String name,	String type) {
			return new EventMetadata<Y>(
					name,
					type,
					clazz,
					true);
		}
		
		public static <Y> EventMetadata<Y> forEvent(Class<Y> clazz, String type) {
			return forEvent(clazz,(String) null,type);
		}
		
		public static HandlerMetadata forHandler(String name, String typeDescription) {
			return new HandlerMetadata(name, typeDescription);
		}
		
		public static HandlerMetadata forHandler(String typeDescription) {
			return new HandlerMetadata(null, typeDescription);
		}
		
		public static HandlerMetadata forHandler(EventHandler<?> instance) {
			return forHandler(
					defaultNameMapper().apply(instance), 
					defaultTypeMapper().apply(instance)
					);
		}
		
		public static uk.co.terminological.pipestream.Metadata forGenerator(String name, String typeDescription) {
			return new uk.co.terminological.pipestream.Metadata(name, typeDescription);
		}
		
		public static uk.co.terminological.pipestream.Metadata forGenerator(String typeDescription) {
			return new uk.co.terminological.pipestream.Metadata(null, typeDescription);
		}
		
		
	}
	
	public static class Events {
		
		public static <Y> Event<Y> defaultEvent(Y instance) {
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
		
		public static <Y> Event<Y> unnamedTypedEvent(Y instance, String type) {
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
		
		public static Predicate<Event<?>> matchNameOf(Event<?> previousEvent) {
			return matchName(s -> s.equals(previousEvent.getMetadata().name().orElse("undefined")));
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
		
		public static Predicate<Event<?>> shutdown() {
			return new Predicate<Event<?>>() {
				@Override
				public boolean test(Event<?> t) {
					return t instanceof Event.Shutdown;
				}
			};
		}
		
		
	}

	public static class Generators {
		
		public static DirectoryScanner directoryScanner(Path directory, FileFilter filter,
				Function<Path,String> nameGenerator,
				Function<Path,String> typeGenerator) {
			return new DirectoryScanner(directory, filter, nameGenerator, typeGenerator);
		}
		
		public static DirectoryScanner directoryScanner(Path directory, FileFilter filter,
				String name,
				String type) {
			return new DirectoryScanner(directory, filter, s -> name, s -> type);
		}
		
		public static <Y> EventGenerator<Y> generator(
					String name,
					String typeDescription,
					Function<EventGenerator<Y>,Y> generator,
					Function<Y,String> nameMapper,
					Function<Y,String> typeMapper
				) {
			return new EventGenerator.Default<Y>(Metadata.forGenerator(name,typeDescription)) {

				@Override
				public List<Event<Y>> generate() {
					return Collections.singletonList(
						Events.namedTypedEvent(
							generator.apply(this), nameMapper, typeMapper)
						);
				}
			};
		}
		
	}
	
	public static class Handlers {
		
		 
		
		public static <X,Y> Adaptor<X,Y> adaptor(
				String name,
				Predicate<Event<?>> acceptEvent,
				Function<X,Y> converter,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper
				) {

			return new Adaptor<X,Y>(name) {

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
		
		public static <X,Y> Adaptor<X,Y> adaptor(
				String name,
				Predicate<Event<?>> acceptEvent,
				BiFunction<X,Adaptor<X,Y>,Y> converter,
				Function<Y,String> nameMapper,
				Function<Y,String> typeMapper
				) {

			return new Adaptor<X,Y>(name) {

				@Override
				public boolean canHandle(Event<?> event) {
					return acceptEvent.test(event);
				}

				@Override
				public Event<Y> convert(X input) {
					return Events.namedTypedEvent(
							converter.apply(input,this),
							nameMapper, typeMapper);
				}
			};
		}
		
		public static <X,Y> Processor<X> processor(
				String name,
				Predicate<Event<?>> acceptEvent,
				BiConsumer<X,Processor<X>> processor
				) {

			return new Processor<X>(name) {

				@Override
				public boolean canHandle(Event<?> event) {
					return acceptEvent.test(event);
				}

				@Override
				public void process(X x, Processor<X> context) {
					processor.accept(x, context);	
				}
			};
		}
		
		
		public static <X> EventProcessor<X> eventProcessor(
				String name,
				Predicate<Event<?>> acceptEvent,
				BiConsumer<Event<X>,EventProcessor<X>> processor
				) {

			return new EventProcessor<X>(name) {

				@Override
				public boolean canHandle(Event<?> event) {
					return acceptEvent.test(event);
				}

				@Override
				public void process(Event<X> event, EventProcessor<X> context) {
					processor.accept(event, context);	
				}
			};
		}
		
		public static <X> Terminal<X> consumer(
				String name,
				Predicate<Event<?>> acceptEvent,
				Consumer<X> consumer
				) {

			return new Terminal<X>(name) {

				@Override
				public boolean canHandle(Event<?> event) {
					return acceptEvent.test(event);
				}

				@Override
				public void consume(X input) {
					consumer.accept(input);
				}
			};
		}
		
		public static <X> FileWriter<X> writer(
				String name,
				Predicate<Event<?>> acceptEvent,
				Function<Event<X>,Path> nameStrategy, 
				EventSerializer<X> serializer
				) {
			return new FileWriter<X>(name, acceptEvent, nameStrategy, serializer);
		}
		
		//TODO: a freemarker based writer might be useful here?
		//TODO: writing to a stream output / like the collector implementation
		//TODO: Collector: partially met dependencies?
		
	}
	
	public static class HandlerGenerators {
		
		public static <Y> EventHandlerGenerator<Event<Y>> create(
				Predicate<Event<?>> acceptEventType,
				Function<Event<Y>,EventHandler<Event<Y>>> handlerBuilder
				) {
			return new EventHandlerGenerator.Default<Event<Y>>() {

				@Override
				public boolean canCreateHandler(Event<?> event) {
					return acceptEventType.test(event);
				}

				@Override
				public EventHandler<Event<Y>> createHandlerFor(Event<Y> indexEvent) {
					return handlerBuilder.apply(indexEvent);
				}
				
			};
		}
		
		
	}
}
