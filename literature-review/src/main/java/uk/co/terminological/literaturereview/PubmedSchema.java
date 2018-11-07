package uk.co.terminological.literaturereview;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

public class PubmedSchema {
	
	public static class Labels {
		public static Label ARTICLE = Label.label("Article");
		public static Label STUB = Label.label("Stub");
		public static Label AUTHOR = Label.label("Author");
		public static Label AFFILIATION = Label.label("Affiliation");
		public static Label MESH_CODE = Label.label("Mesh code");
		public static Label TOKEN = Label.label("Token");
		public static Label SEARCH_RESULT = Label.label("Search result");
		public static Label PDF_AVAILABLE = Label.label("Pdf available");
	}
	
	public enum Rel implements RelationshipType {
	    HAS_AUTHOR, HAS_MESH, HAS_REFERENCE
	}
	
	
	public static void setupSchema(GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    PubmedSchema schema = graph.get().schema();
		    schema.indexFor( ARTICLE ).on( "pmid" ).create();
		    schema.indexFor( ARTICLE ).on( "doi" ).create();
		    schema.constraintFor( ARTICLE ).assertPropertyIsUnique("pmid");
		    schema.constraintFor( ARTICLE ).assertPropertyIsUnique("doi");
		    schema.indexFor( AUTHOR ).on( "identifier" ).create();
		    schema.indexFor( MESH_CODE ).on( "code" ).create();
		    schema.constraintFor( MESH_CODE ).assertPropertyIsUnique("code");
		    tx.success();
		}
	}
	
}
