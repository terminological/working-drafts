package uk.co.terminological.literaturereview;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMed2Neo4jUtils {

	
	public static Label ARTICLE = Label.label("Article"); 
	
	
	public static void setupSchema(GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    Schema schema = graph.get().schema();
		    schema.indexFor( ARTICLE ).on( "pmid" ).create();
		    schema.indexFor( ARTICLE ).on( "doi" ).create();
		    schema.constraintFor(ARTICLE).assertPropertyIsUnique("pmid");
		    schema.constraintFor(ARTICLE).assertPropertyIsUnique("doi");
		    tx.success();
		}
	}
	
	public static void mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
			Node node;
			node = graph.get().findNode(ARTICLE, "pmid", entry.getPMID());
			if (node == null) node = graph.get().createNode(ARTICLE);
			
			
			
		    tx.success();
		}
	}

}
