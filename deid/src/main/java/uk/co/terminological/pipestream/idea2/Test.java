package uk.co.terminological.pipestream.idea2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class Test {
	
	public interface Provider<X> {
		Class<X> getType();
		X provide();
	}
	
	// this should be more like a promise
	// maybe a processing promise.
	
	
	public interface Shape<X> {
		Optional<String> name(String instanceId);
		String typeDescription();
		Class<X> getType();
	}
	
	public interface Pump<X> {
		void register(Consumer consumer);
		List<Consumer> consumers();
		boolean consumersReady();
		
		void primePump(X x);
		boolean sendToConsumers();
		void recall();
		
	}
	
	public interface DataPump<X> extends Pump<X> {
		Shape<X> producesType();
		String instanceId();	
	}
	
	public interface DataSink<X> {
		Shape<X> expectsType();
		String instanceId();
		boolean recieve(X input);
		boolean readyToProcess();
		X get();
	}
	
	public interface Consumer {
		boolean readyToRecieve();
	}
	
	public interface Writer<X> extends DataSink<X>,Consumer {
		
	}
	
	public interface Processor extends Consumer {
		
		String instanceId();
		
		Set<Shape<?>> expectsTypes();
		Set<Class<?>> apiDependencies();
		Set<Shape<?>> producesTypes();
		
		Map<String,DataSink<?>> inputs();
		Map<String,DataPump<?>> outputs();
		
		public Supervisor supervisor();
		public void process();
		
	}
	
	public interface Supervisor {
		List<DataPump<?>> dataSources();
		List<Provider<?>> apiProviders(); 
		
		public Supervisor register(DataPump<?> source);
		public Supervisor register(Writer<?> writer);
		public Supervisor register(Class<Processor> processor);
		public Supervisor register(Provider<?> provider);
		
		void notifyOfSuccess(Processor p);
		void notifyOfFailure(Processor p);
		
		void writeError(String message);
		void handleException(Exception e);
		
		Set<Consumer> checkDependenciesAndCreate();
		void checkDependencies();
	}
	
}
