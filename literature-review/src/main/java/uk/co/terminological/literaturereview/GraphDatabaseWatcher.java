package uk.co.terminological.literaturereview;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents;
import uk.co.terminological.pipestream.Metadata;

public class GraphDatabaseWatcher<Y> extends EventGenerator.Watcher<Y> {

	GraphDatabaseService graph;
	BiConsumer<TransactionData, Watcher<Y>> afterCommit;
	TransactionEventHandler<Void> txListener;
	
	public static final String NEO4J_NODE_WATCHER = "Neo4j node watcher";
	static final String NEO4J_NEW_NODE = "Neo4j node created";
	
	static EventGenerator<List<Long>> newLabelledNodeTrigger(Label label) {
		return newNodeTrigger(node -> node.hasLabel(label), label.name());
	}
	
	static EventGenerator<List<Long>> newNodeTrigger(Predicate<Node> nodeTester, String name) {
		return GraphDatabaseWatcher.create(NEO4J_NODE_WATCHER, 
				(txData, context) -> {
					List<Long> nodelist = new ArrayList<>();
					txData.createdNodes().forEach( node -> {
						if (nodeTester.test(node)) {
							nodelist.add(node.getId());
						}
					});
					context.send(
						FluentEvents.Events.namedTypedEvent(nodelist, name, NEO4J_NEW_NODE)	
					);
				});
	}
	
	public static <Y> GraphDatabaseWatcher<Y> create(String type, BiConsumer<TransactionData, Watcher<Y>> afterCommit) {
		return new GraphDatabaseWatcher<Y>(FluentEvents.Metadata.forGenerator(type), afterCommit);
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
