package uk.co.terminological.literaturereview;

import java.util.Optional;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
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
		    schema.constraintFor( ARTICLE ).assertPropertyIsUnique("pmid");
		    schema.constraintFor( ARTICLE ).assertPropertyIsUnique("doi");
		    schema.indexFor( AUTHOR ).on( "identifier" ).create();
		    tx.success();
		}
	}
	
	public static Optional<Long> mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph) {
		
		Long nodeId = null;
		
		try ( Transaction tx = graph.get().beginTx() )
		{
			Node tmp = entry.getPMID() != null ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID()) : null;
			if (tmp == null) tmp = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "doi", entry.getDoi().get()) : null;
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE);
				tmp.setProperty("pmid", entry.getPMID());
			}
			
			Node node = tmp;
			entry.getDoi().ifPresent(doi -> node.setProperty("doi", doi));
			entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
			node.setProperty("abstract", entry.getAbstract());
			node.setProperty("title", entry.getTitle());
			//entry.getAuthors()
			//entry.getMeshHeadings()
			nodeId = node.getId();
		    tx.success();
		}
		
		return Optional.ofNullable(nodeId);
		
	}

	public static Optional<Long> mapAuthorToNode(Author author, GraphDatabaseApi graph) {
		
Long nodeId = null;
		
		try ( Transaction tx = graph.get().beginTx() )
		{
			Node tmp = graph.get().findNode(AUTHOR, "identifier", author.getIdentifier());
			if (tmp == null) {
				tmp = graph.get().createNode(AUTHOR);
				tmp.setProperty("identifier", author.getIdentifier());
			}
			
			Node node = tmp;
			author.firstName().ifPresent(fn -> node.setProperty("firstName", fn));
			author.lastName().ifPresent(fn -> node.setProperty("lastName", fn));
			author.initials().ifPresent(fn -> node.setProperty("initials", fn));
			//author.affiliations()
			
			nodeId = node.getId();
		    tx.success();
		}
		
		return Optional.ofNullable(nodeId);
		
	}
	
}
