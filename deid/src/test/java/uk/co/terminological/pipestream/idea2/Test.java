package uk.co.terminological.pipestream.idea2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md

public class Test {
	
	public interface Provider<X> {
		Class<X> getType();
		X provide();
		//static Provider<Y> create(Y api);
	}
	
	public interface Shape<X> {
		Optional<String> name(X instance);
		String typeDescription();
		Class<X> getType();
	}
	
	public interface DataPump<X> extends Drone {
		Shape<X> producesType();
		String instanceId();
		
		void register(Consumer consumer);
		List<Consumer> consumers();
		boolean consumersReady();
		boolean pumpPrimed();
		
		void primePump(X x);
		boolean sendToConsumers();
		//void recall();
	}
	
	public interface DataSink<X> extends Drone {
		Shape<X> expectsType();
		String instanceId();
		boolean receive(X input);
		X peek();
		X pop();
		boolean readyToProcess();
	}
	
	public interface Drone {
		public void initialise();
		Supervisor supervisor();
		void shutdown();
		Status reportStatus();
	}
	
	public enum Status {
		OK, BLOCKED, FAIL;
	}
	
	public interface Consumer extends Drone {
		boolean readyToRecieve(String pumpInstanceId);
	}
	
	public interface Reader<X> extends DataPump<X> {
		public void read();
	}
	
	public interface Writer<X> extends DataSink<X>,Consumer {
		public void write();
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
