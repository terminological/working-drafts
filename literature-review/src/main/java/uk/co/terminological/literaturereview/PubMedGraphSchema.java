package uk.co.terminological.literaturereview;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubMedGraphSchema {
	
	public static Logger logger = LoggerFactory.getLogger(PubMedGraphSchema.class);
	
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
	
	public static class Prop {
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
		    schema.indexFor( Labels.ARTICLE ).on( Prop.PMID ).create();
		    schema.indexFor( Labels.ARTICLE ).on( Prop.DOI ).create();
		    schema.indexFor( Labels.ARTICLE ).on( Prop.PMCID ).create();
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Prop.PMID );
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Prop.DOI );
		    schema.constraintFor( Labels.ARTICLE ).assertPropertyIsUnique( Prop.PMCID );
		    //schema.indexFor( Labels.AUTHOR ).on( Props.AUTHOR_ID ).create();
		    schema.indexFor( Labels.TOKEN ).on( Prop.TOKEN_VALUE ).create();
		    schema.indexFor( Labels.MESH_CODE ).on( Prop.MESH_CODE ).create();
		    schema.constraintFor( Labels.MESH_CODE ).assertPropertyIsUnique( Prop.MESH_CODE );
		    tx.success();
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}
	
}
