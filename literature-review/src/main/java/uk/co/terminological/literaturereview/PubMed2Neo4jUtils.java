package uk.co.terminological.literaturereview;

import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

public class PubMed2Neo4jUtils {

	
	public static Label ARTICLE = Label.label("Article"); 
	public static Label AUTHOR = Label.label("Author");
	public static Label AFFILIATION = Label.label("Affiliation");
	public static Label MESH_CODE = Label.label("Mesh code");
	public static Label TOKEN = Label.label("Token");
	
	public enum Rel implements RelationshipType {
	    HAS_AUTHOR, HAS_MESH
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
		    schema.indexFor( MESH_CODE ).on( "code" ).create();
		    schema.constraintFor( MESH_CODE ).assertPropertyIsUnique("code");
		    tx.success();
		}
	}
	
	public static Optional<Node> mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph) {
		
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = entry.getPMID() != null ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID()) : null;
			if (tmp == null) tmp = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "doi", entry.getDoi().get()) : null;
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE);
			}
			
			Node node = tmp;
			entry.getPMID().ifPresent(pmid -> node.setProperty("pmid", pmid));
			entry.getDoi().ifPresent(doi -> node.setProperty("doi", doi));
			entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
			node.setProperty("abstract", entry.getAbstract());
			node.setProperty("title", entry.getTitle());
			entry.getAuthors().forEach(au -> {
				Optional<Node> targetNode = mapAuthorToNode(au,graph);
				targetNode.ifPresent(target -> node.createRelationshipTo(target, Rel.HAS_AUTHOR));
			});
			entry.getMeshHeadings().forEach(mh -> {
				Optional<Node> targetNode = mapMeshCodeToNode(mh.getDescriptor(),graph);
				targetNode.ifPresent(target -> node.createRelationshipTo(target, Rel.HAS_MESH));
			});
			out = node;
		    tx.success();
		}
		
		return Optional.ofNullable(out);
		
	}

	public static Optional<Node> mapAuthorToNode(Author author, GraphDatabaseApi graph) {
		
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			
			Node tmp = graph.get().findNode(AUTHOR, "identifier", author.getIdentifier());
			if (tmp == null) {
				tmp = graph.get().createNode(AUTHOR);
				tmp.setProperty("identifier", author.getIdentifier());
			}
			
			Node node = tmp;
			author.firstName().ifPresent(fn -> node.setProperty("firstName", fn));
			author.lastName().ifPresent(fn -> node.setProperty("lastName", fn));
			author.initials().ifPresent(fn -> node.setProperty("initials", fn));
			String[] affiliations = author.affiliations().collect(Collectors.toList()).toArray(new String[] {});
			if (affiliations.length > 0) node.setProperty("affiliations", affiliations);
			
			out = node;
		    tx.success();
		    
		}
		
		return Optional.ofNullable(out);
		
	}
	
	public static Optional<Node> mapMeshCodeToNode(MeshCode meshCode, GraphDatabaseApi graph) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			
			Node tmp = graph.get().findNode(MESH_CODE, "code", meshCode.getCode());
			if (tmp == null) {
				tmp = graph.get().createNode(MESH_CODE);
				tmp.setProperty("code", meshCode.getCode());
				tmp.setProperty("term", meshCode.getTerm());
			}
			out = tmp;
		    tx.success();
		    
		}
		
		return Optional.ofNullable(out);
	}
	
}
