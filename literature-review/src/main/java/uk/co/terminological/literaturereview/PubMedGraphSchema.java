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
		public static Label PMCENTRAL_STUB = Label.label("PMCentralStub");
		public static Label ORIGINAL_SEARCH = Label.label("PubmedSearchResult");
		public static Label EXPAND = Label.label("Expand");
		public static Label AUTHOR = Label.label("Author");
		public static Label AFFILIATION = Label.label("Affiliation");
		public static Label MESH_CODE = Label.label("MeshCode");
		public static Label TOKEN = Label.label("Token");
		public static Label SEARCH_RESULT = Label.label("SearchResult");
		public static Label PDF_AVAILABLE = Label.label("PdfAvailable");
	}
	
	public enum Rel implements RelationshipType {
	    HAS_AUTHOR, HAS_MESH, HAS_REFERENCE, HAS_RELATED, HAS_TOKEN, TOKEN_PRECEEDED_BY
	}
	
	public static class Props {
		public static String PMID = "pmid";
		public static String DOI = "doi";
		public static String PMCID = "pmcid";
		public static String AUTHOR_ID = "identifier";
		public static String MESH_CODE = "code";
		public static String TOKEN_VALUE = "value";
		
	}
	
	public static void setupSchema(GraphDatabaseApi graph) {
		
		try ( Transaction tx = graph.get().beginTx() )
		{
		    Schema schema = graph.get().schema();
		    schema.indexFor( Labels.ARTICLE ).on( Props.PMID ).create();
		    schema.indexFor( Labels.ARTICLE ).on( Props.DOI ).create();
		    schema.indexFor( Labels.ARTICLE ).on( Props.PMCID ).create();
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Props.PMID );
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Props.DOI );
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Props.PMCID );
		    //schema.indexFor( Labels.AUTHOR ).on( Props.AUTHOR_ID ).create();
		    schema.indexFor( Labels.TOKEN ).on( Props.TOKEN_VALUE ).create();
		    schema.indexFor( Labels.MESH_CODE ).on( Props.MESH_CODE ).create();
		    schema.constraintFor( Labels.MESH_CODE ).assertPropertyIsUnique( Props.MESH_CODE );
		    tx.success();
		}
	}
	
}
