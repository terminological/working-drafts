package uk.co.terminological.literaturereview;

import java.util.Optional;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMed2Neo4jUtils {

	
	public static Label ARTICLE = Label.label("Article"); 
	public static Label AUTHOR = Label.label("Author");
	public static Label AFFILIATION = Label.label("Affiliation");
	public static Label TOKEN = Label.label("Token");
	
	public enum Rel implements RelationshipType {
	    KNOWS
	}
	
	
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
	
	public static Optional<Long> mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph) {
		
		Long nodeId = null;
		
		try ( Transaction tx = graph.get().beginTx() )
		{
			Node tmp = graph.get().findNode(ARTICLE, "pmid", entry.getPMID());
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE);
				tmp.setProperty("pmid", entry.getPMID());
			}
			
			Node node = tmp;
			entry.getDoi().ifPresent(doi -> node.setProperty("doi", doi));
			node.setProperty("abstract", entry.getAbstract());
			
			
			nodeId = node.getId();
		    tx.success();
		}
		
		return Optional.ofNullable(nodeId);
		
	}

}
