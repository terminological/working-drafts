package uk.co.terminological.pipestream.idea2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Test {
	
	public interface Provider<X> {
		Class<X> getType();
		X provide();
		//static Provider<Y> create(Y api);
	}
	
	public interface Shape<X> {
		Optional<String> name(String instanceId);
		String typeDescription();
		Class<X> getType();
	}
	
	public interface DataPump<X> extends Drone {
		Shape<X> producesType();
		String instanceId();
		
		void register(Consumer consumer);
		List<Consumer> consumers();
		boolean consumersReady();
		
		void primePump(X x);
		boolean sendToConsumers();
		void recall();
	}
	
	public interface DataSink<X> extends Drone {
		Shape<X> expectsType();
		String instanceId();
		boolean recieve(X input);
		boolean readyToProcess();
		X get();
	}
	
	public interface Drone {
		Supervisor supervisor();
		void shutdown();
		Status reportStatus();
	}
	
	public enum Status {
		OK, BLOCKED, FAIL;
	}
	
	public interface Consumer extends Drone {
		boolean readyToRecieve();
	}
	
	public interface Writer<X> extends DataSink<X>,Consumer {
		public void initialise();
		public void write();
		public void shutdown();
	}
	
	public interface Processor extends Consumer {
		
		String instanceId();
		
		Set<Shape<?>> expectsTypes();
		Set<Class<?>> apiDependencies();
		Set<Shape<?>> producesTypes();
		
		Map<String,DataSink<?>> inputs();
		Map<String,DataPump<?>> outputs();
		Map<Class<?>,Provider<?>> apis();
		
		public <X> X getApi(Class<X> apiType);
		
		public Supervisor supervisor();
		public void process();
		public void shutdown();
		
	}
	
	public interface Supervisor {
		
		List<DataPump<?>> dataSources();
		List<Provider<?>> apiProviders();
		List<Consumer> consumers();
		
		public Supervisor register(DataPump<?> source);
		public Supervisor register(Class<? extends Consumer> consumers);
		public Supervisor register(Provider<?> provider);
		
		void notifyOfSuccess(Processor p);
		void notifyOfFailure(Processor p);
		
		void logInfo(String message);
		void logError(String message);
		void handleException(Exception e);
		
		void checkDependencies();
		void createConsumers();
		void crankHandle();
		
	}
	
}
