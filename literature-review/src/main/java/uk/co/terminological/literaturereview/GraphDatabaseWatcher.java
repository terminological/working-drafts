package uk.co.terminological.literaturereview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pipestream.EventGenerator;
import uk.co.terminological.pipestream.FluentEvents;
import uk.co.terminological.pipestream.Metadata;

public class GraphDatabaseWatcher<Y> extends EventGenerator.Watcher<Y> {

	private static final Logger logger = LoggerFactory.getLogger(GraphDatabaseWatcher.class);
	
	GraphDatabaseService graph;
	BiConsumer<TransactionData, Watcher<Y>> afterCommit;
	TransactionEventHandler<Void> txListener;
	
	public static final String NEO4J_NODE_WATCHER = "Neo4j node watcher";
	public static final String NEO4J_NEW_NODE = "Neo4j node created";
	
	static EventGenerator<Set<Long>> newLabelTrigger(Label label) {
		return GraphDatabaseWatcher.create(NEO4J_NODE_WATCHER, 
				(txData, context) -> {
					Set<Long> nodelist = new HashSet<>();
					txData.assignedLabels().forEach( labelledNode -> {
						if (labelledNode.label().equals(label)) {
							nodelist.add(labelledNode.node().getId());
						}
					});
					context.send(
						FluentEvents.Events.namedTypedEvent(nodelist, label.name(), NEO4J_NEW_NODE)	
					);
				});
	}
	
	static EventGenerator<Set<Long>> newNodeTrigger(Predicate<Node> nodeTester, String name) {
		return GraphDatabaseWatcher.create(NEO4J_NODE_WATCHER, 
				(txData, context) -> {
					Set<Long> nodelist = new HashSet<>();
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
				new Thread(() -> {
					logger.debug("Post transaction hook fired");
					afterCommit.accept(data, GraphDatabaseWatcher.this);
					});
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
