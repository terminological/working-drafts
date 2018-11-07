package uk.co.terminological.literaturereview;

import java.util.function.BiConsumer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents;
import uk.co.terminological.pipestream.Metadata;

public class GraphDatabaseWatcher<Y> extends EventGenerator.Watcher<Y> {

	GraphDatabaseService graph;
	BiConsumer<TransactionData, Watcher<Y>> afterCommit;
	TransactionEventHandler<Void> txListener;
	
	public static <Y> GraphDatabaseWatcher<Y> create(String name, String type, BiConsumer<TransactionData, Watcher<Y>> afterCommit) {
		return new GraphDatabaseWatcher<Y>(FluentEvents.Metadata.forGenerator(name, type), afterCommit);
	}
	
	public GraphDatabaseWatcher(Metadata metadata, GraphDatabaseService graph, BiConsumer<TransactionData, Watcher<Y>> afterCommit) {
		super(metadata);
		this.graph = graph;
		this.afterCommit = afterCommit;
	}

	public GraphDatabaseWatcher(Metadata metadata, BiConsumer<TransactionData, Watcher<Y>> afterCommit) {
		super(metadata);
		this.graph = this.getEventBus().getApi(GraphDatabaseApi.class).get().get();
		this.afterCommit = afterCommit;
	}
	
	@Override
	public Object setupWatcher() {
		txListener = new TransactionEventHandler<Void>() {

			@Override
			public Void beforeCommit(TransactionData data) throws Exception { return null; }

			@Override
			public void afterCommit(TransactionData data, Void state) {
				afterCommit.accept(data, GraphDatabaseWatcher.this);
			}

			@Override
			public void afterRollback(TransactionData data, Void state) {}
			
		};
		graph.registerTransactionEventHandler(txListener);
		return null;
	}

	public void tearDownWatcher() {
		graph.unregisterTransactionEventHandler(txListener);
	}

}
