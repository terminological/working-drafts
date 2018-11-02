package uk.co.terminological.literaturereview;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMed2Neo4jUtils {

	
	public static Label ARTICLE = Label.label("Article"); 
	
	
	IndexDefinition pmidIndex;
	IndexDefinition doiIndex;
	
	public void setupSchema(GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    Schema schema = graph.get().schema();
		    pmidIndex = schema.indexFor( ARTICLE ).on( "pmid" ).create();
		    doiIndex = schema.indexFor( ARTICLE ).on( "doi" ).create();
		    tx.success();
		}
	}
	
	public void mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    // Database operations go here
			graph.get().findNode(ARTICLE, "pmid", entry.getPMID())
			Node node = graph.get().createNode(ARTICLE);
		    tx.success();
		}
	}

}
