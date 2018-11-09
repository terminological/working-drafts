package uk.co.terminological.literaturereview;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

public class PubMedGraphSchema {
	
	public static class Labels {
		public static Label ARTICLE = Label.label("Article");
		public static Label DOI_STUB = Label.label("DOIStub");
		public static Label PMID_STUB = Label.label("PMIDStub");
		public static Label EXPAND = Label.label("Expand");
		public static Label AUTHOR = Label.label("Author");
		public static Label AFFILIATION = Label.label("Affiliation");
		public static Label MESH_CODE = Label.label("MeshCode");
		public static Label TOKEN = Label.label("Token");
		public static Label SEARCH_RESULT = Label.label("SearchResult");
		public static Label PDF_AVAILABLE = Label.label("PdfAvailable");
	}
	
	public enum Rel implements RelationshipType {
	    HAS_AUTHOR, HAS_MESH, HAS_REFERENCE, HAS_RELATED
	}
	
	
	public static void setupSchema(GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    Schema schema = graph.get().schema();
		    schema.indexFor( Labels.ARTICLE ).on( "pmid" ).create();
		    schema.indexFor( Labels.ARTICLE ).on( "doi" ).create();
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique("pmid");
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique("doi");
		    schema.indexFor( Labels.AUTHOR ).on( "identifier" ).create();
		    schema.indexFor( Labels.MESH_CODE ).on( "code" ).create();
		    schema.constraintFor( Labels.MESH_CODE ).assertPropertyIsUnique("code");
		    tx.success();
		}
	}
	
}
